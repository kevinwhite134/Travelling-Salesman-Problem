package org.example.controller;

import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.control.*; // includes SplitPane
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.example.model.HullOrderChecker;
import org.example.model.PointNode;
import org.example.model.TSPBruteForce;
import org.example.model.TSPHeuristic;
import org.example.view.TSPCanvas;

import java.util.*;

public class TSPController {

    private static final int MAX_BRUTE_FORCE_POINTS = 11;

    private final BorderPane root = new BorderPane();

    private final TSPCanvas left = new TSPCanvas(520, 520);   // brute
    private final TSPCanvas right = new TSPCanvas(520, 520);  // heuristic

    private final Slider nSlider = new Slider(3, 20, 8);
    private final Label nLabel = new Label();

    private final CheckBox manualMode = new CheckBox("Manual placement");

    private final CheckBox showComCenteredCircles = new CheckBox("Show COM-centered circles");
    private final CheckBox showAverageComCircle = new CheckBox("Show average COM circle");
    private final CheckBox showRmsComCircle = new CheckBox("Show RMS COM circle");
    private final CheckBox extendRays = new CheckBox("Extend rays through COM");
    private final CheckBox showAngles = new CheckBox("Show angles");
    private final CheckBox highlightHullPoints = new CheckBox("Hull points");
    private final CheckBox showHullPath = new CheckBox("Hull path");
    private final CheckBox spikeReorder = new CheckBox("Spike reorder");

    private final Button solveBtn = new Button("Solve");
    private final Button clearBtn = new Button("Clear points");
    private final Button regenBtn = new Button("Regenerate");

    private final Label bruteLabel = new Label("Brute: -");
    private final Label heurLabel = new Label("Heuristic: -");

    private final Label bruteMeta = new Label("");
    private final Label heurMeta = new Label("");

    private final Random rng = new Random();
    private List<PointNode> points = new ArrayList<>();

    public TSPController() {
        buildUI();
        wireManualPlacement();
    }

    public Parent getRoot() {
        return root;
    }

    public void init() {
        regenerate();
    }

    private void buildUI() {
        nSlider.setMajorTickUnit(1);
        nSlider.setMinorTickCount(0);
        nSlider.setSnapToTicks(true);
        nSlider.setShowTickMarks(true);
        nSlider.setShowTickLabels(true);

        nSlider.valueProperty().addListener((obs, oldV, newV) -> nLabel.setText("Points: " + newV.intValue()));
        nLabel.setText("Points: " + (int) nSlider.getValue());

        nSlider.setOnMouseReleased(e -> {
            if (!manualMode.isSelected()) regenerate();
        });

        manualMode.setStyle("-fx-text-fill: #ddd;");
        showComCenteredCircles.setStyle("-fx-text-fill: #ddd;");
        showAverageComCircle.setStyle("-fx-text-fill: #ddd;");
        showRmsComCircle.setStyle("-fx-text-fill: #ddd;");
        extendRays.setStyle("-fx-text-fill: #ddd;");
        showAngles.setStyle("-fx-text-fill: #ddd;");
        highlightHullPoints.setStyle("-fx-text-fill: #ddd;");
        showHullPath.setStyle("-fx-text-fill: #ddd;");
        spikeReorder.setStyle("-fx-text-fill: #ddd;");

        showComCenteredCircles.selectedProperty().addListener((obs, ov, nv) -> solveCurrentPoints());
        showAverageComCircle.selectedProperty().addListener((obs, ov, nv) -> solveCurrentPoints());
        showRmsComCircle.selectedProperty().addListener((obs, ov, nv) -> solveCurrentPoints());
        extendRays.selectedProperty().addListener((obs, ov, nv) -> solveCurrentPoints());
        showAngles.selectedProperty().addListener((obs, ov, nv) -> solveCurrentPoints());
        highlightHullPoints.selectedProperty().addListener((obs, ov, nv) -> solveCurrentPoints());
        showHullPath.selectedProperty().addListener((obs, ov, nv) -> solveCurrentPoints());
        spikeReorder.selectedProperty().addListener((obs, ov, nv) -> solveCurrentPoints());

        manualMode.selectedProperty().addListener((obs, oldV, newV) -> {
            if (newV) {
                regenBtn.setDisable(true);
                nSlider.setDisable(true);
            } else {
                regenBtn.setDisable(false);
                nSlider.setDisable(false);
                regenerate();
            }
        });

        regenBtn.setOnAction(e -> regenerate());
        clearBtn.setOnAction(e -> clearManual());
        solveBtn.setOnAction(e -> solveCurrentPoints());

        FlowPane top = new FlowPane(12, 8);
        top.getChildren().addAll(
                nLabel, nSlider,
                regenBtn,
                manualMode,
                showComCenteredCircles,
                showAverageComCircle,
                showRmsComCircle,
                extendRays,
                showAngles,
                highlightHullPoints,
                showHullPath,
                spikeReorder,
                solveBtn,
                clearBtn
        );
        top.setPadding(new Insets(10));
        top.setStyle("-fx-background-color: #0f0f0f; -fx-border-color: #2b2b2b; -fx-border-width: 0 0 1 0;");
        nLabel.setStyle("-fx-text-fill: #ddd;");
        nSlider.setPrefWidth(180);

        bruteLabel.setStyle("-fx-text-fill: #ddd; -fx-font-size: 18;");
        heurLabel.setStyle("-fx-text-fill: #ddd; -fx-font-size: 18;");
        bruteMeta.setStyle("-fx-text-fill: #bbb; -fx-font-size: 14;");
        heurMeta.setStyle("-fx-text-fill: #bbb; -fx-font-size: 14;");

        VBox leftBox = new VBox(6,
                styledTitle("Brute Force (optimal for small N)"),
                left,
                bruteLabel,
                bruteMeta
        );

        VBox rightBox = new VBox(6,
                styledTitle("Heuristic (angle around COM)"),
                right,
                heurLabel,
                heurMeta
        );

        leftBox.setPadding(new Insets(10));
        rightBox.setPadding(new Insets(10));
        leftBox.setStyle("-fx-background-color: #0b0b0b;");
        rightBox.setStyle("-fx-background-color: #0b0b0b;");
        leftBox.setMinWidth(560);
        rightBox.setMinWidth(560);

        SplitPane split = new SplitPane(leftBox, rightBox);
        split.setDividerPositions(0.5);
        split.setMinWidth(1120);

        ScrollPane scroll = new ScrollPane(split);
        scroll.setFitToHeight(true);
        scroll.setPannable(true);
        scroll.setStyle("-fx-background: #0b0b0b; -fx-background-color: #0b0b0b;");

        root.setTop(top);
        root.setCenter(scroll);
        root.setStyle("-fx-background-color: #0b0b0b;");
    }

    private Label styledTitle(String s) {
        Label l = new Label(s);
        l.setStyle("-fx-text-fill: #ddd; -fx-font-size: 14;");
        return l;
    }

    private void wireManualPlacement() {
        left.setOnLeftClickAdd(p -> {
            if (!manualMode.isSelected()) return;
            addManualPoint(p);
        });
        right.setOnLeftClickAdd(p -> {
            if (!manualMode.isSelected()) return;
            addManualPoint(p);
        });

        left.setOnRightClick(p -> {
            if (!manualMode.isSelected()) return;
            eraseNearestPoint(p);
        });
        right.setOnRightClick(p -> {
            if (!manualMode.isSelected()) return;
            eraseNearestPoint(p);
        });
    }

    private void addManualPoint(Point2D p) {
        double pad = 10;
        double x = Math.max(pad, Math.min(510 - pad, p.getX()));
        double y = Math.max(pad, Math.min(510 - pad, p.getY()));

        PointNode node = new PointNode(x, y);

        left.addPoint(node);
        right.addPoint(node);

        points = right.getPoints();

        solveCurrentPoints();
    }

    private void eraseNearestPoint(Point2D click) {
        if (points == null || points.isEmpty()) return;

        int bestIndex = -1;
        double bestDist = Double.MAX_VALUE;

        for (int i = 0; i < points.size(); i++) {
            PointNode pt = points.get(i);
            double dx = pt.x() - click.getX();
            double dy = pt.y() - click.getY();
            double d = Math.sqrt(dx * dx + dy * dy);
            if (d < bestDist) {
                bestDist = d;
                bestIndex = i;
            }
        }

        double threshold = 18;
        if (bestIndex != -1 && bestDist <= threshold) {
            points.remove(bestIndex);

            left.clearAll();
            right.clearAll();
            left.setPoints(points);
            right.setPoints(points);

            solveCurrentPoints();
        }
    }

    private void clearManual() {
        points.clear();

        left.clearPointsOnly();
        right.clearPointsOnly();

        bruteLabel.setText("Brute: -");
        heurLabel.setText("Heuristic: -");
        bruteMeta.setText("");
        heurMeta.setText("");
    }

    private void regenerate() {
        if (manualMode.isSelected()) return;

        int n = (int) nSlider.getValue();
        points = randomPoints(n, 500, 500, 20);

        left.clearAll();
        right.clearAll();

        left.setPoints(points);
        right.setPoints(points);

        solveCurrentPoints();
    }

    private void solveCurrentPoints() {
        if (points == null || points.size() < 3) {
            heurLabel.setText("Heuristic length: - (need 3+ points)");
            bruteLabel.setText("Brute length: - (need 3+ points)");

            left.clearComCenteredCircles();
            right.clearComCenteredCircles();
            left.clearAverageComCircle();
            right.clearAverageComCircle();
            left.clearRmsComCircle();
            right.clearRmsComCircle();
            left.clearAverageIntersections();
            right.clearAverageIntersections();
            left.clearRmsIntersections();
            right.clearRmsIntersections();
            left.clearAngleLabels();
            right.clearAngleLabels();
            left.clearConvexHullOverlay();
            right.clearConvexHullOverlay();
            return;
        }

        Point2D com = centreOfMass(points);
        double r = enclosingRadius(points, com);
        double avgRadius = averageRadiusToCom(points, com);
        double rmsRadius = rmsRadiusToCom(points, com);
        boolean extend = extendRays.isSelected();

        left.clearAll();
        right.clearAll();
        left.setPoints(points);
        right.setPoints(points);

        left.setOuterCircle(com, r);
        left.setComDot(com);
        left.setRaysFromCom(com, r, extend);

        right.setOuterCircle(com, r);
        right.setComDot(com);
        right.setRaysFromCom(com, r, extend);

        if (showComCenteredCircles.isSelected()) {
            left.showComCenteredCircles(com);
            right.showComCenteredCircles(com);
        } else {
            left.clearComCenteredCircles();
            right.clearComCenteredCircles();
        }

        TSPHeuristic.Result heurRes = TSPHeuristic.solveByAngle(points, com, spikeReorder.isSelected());
        HullOrderChecker.CheckResult hullCheck = HullOrderChecker.check(points, heurRes.order);
        System.out.printf("HullOrder[Heuristic]: %s (n=%d, hullCorners=%d)%n",
                hullCheck.follows() ? "PASS" : "FAIL",
                points.size(),
                hullCheck.hullCorners());
        double heurLen = closedTourLength(points, heurRes.order);

        int n = points.size();
        if (n <= MAX_BRUTE_FORCE_POINTS) {
            TSPBruteForce.Result bruteRes = TSPBruteForce.solve(points);
            double bestLen = bruteRes.length;

            left.setTourOrderColor(bruteRes.order, true, Color.LIMEGREEN);

            Set<Long> bruteEdges = buildUndirectedEdgeSet(bruteRes.order, true);
            right.setTourOrderCompare(
                    heurRes.order,
                    true,
                    (u, v) -> bruteEdges.contains(edgeKey(u, v)),
                    Color.LIMEGREEN,
                    Color.RED
            );

            applyCircleOverlaysAndIntersections(com, avgRadius, rmsRadius, bruteRes.order, heurRes.order, true);

            double off = heurLen - bestLen;
            double pct = (bestLen == 0) ? 0 : (off / bestLen) * 100.0;

            heurLabel.setText(String.format("Heuristic length: %.2f   (off: %.2f, %.2f%%)", heurLen, off, pct));
            bruteLabel.setText(String.format("Brute length: %.2f", bestLen));

            heurMeta.setText(metaText(heurRes.bigO, heurRes.elapsedNanos, heurRes.operations));
            bruteMeta.setText(metaText(bruteRes.bigO, bruteRes.elapsedNanos, bruteRes.operations));

            if (showAngles.isSelected()) {
                left.showAngles(com, bruteRes.order);
                right.showAngles(com, heurRes.order);
            } else {
                left.clearAngleLabels();
                right.clearAngleLabels();
            }

            applyConvexHullOverlay();

        } else {
            right.setTourOrderColor(heurRes.order, true, Color.RED);
            left.setTourOrder(List.of(), true);

            applyCircleOverlaysAndIntersections(com, avgRadius, rmsRadius, List.of(), heurRes.order, false);

            bruteLabel.setText("Brute length: (disabled for N > " + MAX_BRUTE_FORCE_POINTS + ")");
            bruteMeta.setText("Big-O: O(n!)   Time: -   Ops: -");

            heurLabel.setText(String.format("Heuristic length: %.2f   (off: N/A)", heurLen));
            heurMeta.setText(metaText(heurRes.bigO, heurRes.elapsedNanos, heurRes.operations));

            if (showAngles.isSelected()) {
                left.clearAngleLabels();
                right.showAngles(com, heurRes.order);
            } else {
                left.clearAngleLabels();
                right.clearAngleLabels();
            }

            applyConvexHullOverlay();
        }
    }

    private void applyConvexHullOverlay() {
        boolean highlight = highlightHullPoints.isSelected();
        boolean connect = showHullPath.isSelected();
        if (!highlight && !connect) {
            left.clearConvexHullOverlay();
            right.clearConvexHullOverlay();
            return;
        }

        List<Integer> hullOrder = HullOrderChecker.convexHullCorners(points);
        left.showConvexHullOverlay(hullOrder, highlight, connect);
        right.showConvexHullOverlay(hullOrder, highlight, connect);
    }

    private void applyCircleOverlaysAndIntersections(Point2D com,
                                                     double avgRadius,
                                                     double rmsRadius,
                                                     List<Integer> leftOrder,
                                                     List<Integer> rightOrder,
                                                     boolean leftHasTour) {
        if (showAverageComCircle.isSelected()) {
            left.showAverageComCircle(com, avgRadius);
            right.showAverageComCircle(com, avgRadius);

            if (leftHasTour) {
                left.showAverageIntersections(leftOrder, true, com, avgRadius);
            } else {
                left.clearAverageIntersections();
            }
            right.showAverageIntersections(rightOrder, true, com, avgRadius);
        } else {
            left.clearAverageComCircle();
            right.clearAverageComCircle();
            left.clearAverageIntersections();
            right.clearAverageIntersections();
        }

        if (showRmsComCircle.isSelected()) {
            left.showRmsComCircle(com, rmsRadius);
            right.showRmsComCircle(com, rmsRadius);

            if (leftHasTour) {
                left.showRmsIntersections(leftOrder, true, com, rmsRadius);
            } else {
                left.clearRmsIntersections();
            }
            right.showRmsIntersections(rightOrder, true, com, rmsRadius);
        } else {
            left.clearRmsComCircle();
            right.clearRmsComCircle();
            left.clearRmsIntersections();
            right.clearRmsIntersections();
        }
    }

    private String metaText(String bigO, long nanos, long ops) {
        double ms = nanos / 1_000_000.0;
        return String.format("Big-O: %s   Time: %.3f ms   Ops: %,d", bigO, ms, ops);
    }

    private static long edgeKey(int a, int b) {
        int u = Math.min(a, b);
        int v = Math.max(a, b);
        return (((long) u) << 32) ^ (v & 0xffffffffL);
    }

    private static Set<Long> buildUndirectedEdgeSet(List<Integer> order, boolean closedLoop) {
        Set<Long> set = new HashSet<>();
        if (order == null || order.size() < 2) return set;

        for (int i = 0; i < order.size() - 1; i++) {
            set.add(edgeKey(order.get(i), order.get(i + 1)));
        }
        if (closedLoop && order.size() > 2) {
            set.add(edgeKey(order.get(order.size() - 1), order.get(0)));
        }
        return set;
    }

    private List<PointNode> randomPoints(int n, double w, double h, double pad) {
        List<PointNode> pts = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            double x = pad + rng.nextDouble() * (w - 2 * pad);
            double y = pad + rng.nextDouble() * (h - 2 * pad);
            pts.add(new PointNode(x, y));
        }
        return pts;
    }

    private Point2D centreOfMass(List<PointNode> pts) {
        double sx = 0, sy = 0;
        for (PointNode p : pts) {
            sx += p.x();
            sy += p.y();
        }
        return new Point2D(sx / pts.size(), sy / pts.size());
    }

    private double enclosingRadius(List<PointNode> pts, Point2D com) {
        double max = 0;
        for (PointNode p : pts) {
            double dx = p.x() - com.getX();
            double dy = p.y() - com.getY();
            double d = Math.sqrt(dx * dx + dy * dy);
            if (d > max) max = d;
        }
        return max + 12;
    }

    private double averageRadiusToCom(List<PointNode> pts, Point2D com) {
        if (pts == null || pts.isEmpty()) return 0;
        double sum = 0;
        for (PointNode p : pts) {
            double dx = p.x() - com.getX();
            double dy = p.y() - com.getY();
            sum += Math.sqrt(dx * dx + dy * dy);
        }
        return sum / pts.size();
    }

    private double rmsRadiusToCom(List<PointNode> pts, Point2D com) {
        if (pts == null || pts.isEmpty()) return 0;
        double sumSq = 0;
        for (PointNode p : pts) {
            double dx = p.x() - com.getX();
            double dy = p.y() - com.getY();
            sumSq += dx * dx + dy * dy;
        }
        return Math.sqrt(sumSq / pts.size());
    }

    private double closedTourLength(List<PointNode> pts, List<Integer> order) {
        if (order.size() < 2) return 0;
        double total = 0;
        for (int i = 0; i < order.size() - 1; i++) {
            total += dist(pts.get(order.get(i)), pts.get(order.get(i + 1)));
        }
        total += dist(pts.get(order.get(order.size() - 1)), pts.get(order.get(0)));
        return total;
    }

    private double dist(PointNode a, PointNode b) {
        double dx = a.x() - b.x();
        double dy = a.y() - b.y();
        return Math.sqrt(dx * dx + dy * dy);
    }
}
