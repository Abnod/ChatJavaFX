package abnod.chaterr.client;

import javafx.scene.Parent;
import javafx.stage.Stage;

class MoveAndResizeController {

    private Stage stage;
    private Parent parent;

    private double deltaX = 0;
    private double deltaY = 0;
    private double windowWidth = 0;
    private double windowHeight = 0;
    private double windowX;
    private double windowY;
    private Direction direction;

    private enum Direction {
        N, S, E, W, NE, NW, SE, SW, MOVE
    }

    MoveAndResizeController(Stage stage, Parent parent) {
        this.parent = parent;
        this.stage = stage;

        moveListener();
        resizeListener();
    }

    private void moveListener() {
        parent.setOnMousePressed(event -> {
            windowX = stage.getX();
            windowY = stage.getY();
            double clickX = event.getScreenX();
            double clickY = event.getScreenY();
            deltaX = windowX - clickX;
            deltaY = windowY - clickY;
            windowWidth = stage.getWidth();
            windowHeight = stage.getHeight();

            direction = checkDirection(clickX, clickY, windowX, windowY);
        });
    }

    private Direction checkDirection(double clickX, double clickY, double windowX, double windowY) {
        if (clickX <= windowX + 5 && clickX >= windowX - 5) {
            if (clickY <= windowY + 5 && clickY >= windowY - 5) {
                return Direction.NW;
            } else if (clickY <= windowY + windowHeight + 5 && clickY >= windowY + windowHeight - 5) {
                return Direction.SW;
            } else {
                return Direction.W;
            }
        } else if (clickX <= windowX + windowWidth + 5 && clickX >= windowX + windowWidth - 5) {
            if (clickY <= windowY + 5 && clickY >= windowY - 5) {
                return Direction.NE;
            } else if (clickY <= windowY + windowHeight + 5 && clickY >= windowY + windowHeight - 5) {
                return Direction.SE;
            } else {
                return Direction.E;
            }
        } else if (clickY <= windowY + 5 && clickY >= windowY - 5) {
            return Direction.N;
        } else if (clickY <= windowY + windowHeight + 5 && clickY >= windowY + windowHeight - 5) {
            return Direction.S;
        } else return Direction.MOVE;
    }

    private void resizeListener() {
        parent.setOnMouseDragged(event -> {
            switch (direction) {
                case N: {
                    if (windowHeight > stage.getMinHeight() || event.getY() < 0) {
                        windowHeight -= event.getScreenY() - stage.getY();
                        stage.setHeight(windowHeight);
                        stage.setY(event.getScreenY());
                    }
                    break;
                }
                case S: {
                    if (windowHeight > stage.getMinHeight() || event.getY() - windowHeight > 0) {
                        windowHeight += event.getY() - windowHeight;
                        stage.setHeight(windowHeight);
                    }
                    break;
                }
                case E: {
                    if (windowWidth > stage.getMinWidth() || event.getX() - windowWidth > 0) {
                        windowWidth += event.getX() - windowWidth;
                        stage.setWidth(windowWidth);
                    }
                    break;
                }
                case W: {
                    if (windowWidth > stage.getMinWidth() || event.getX() < 0) {
                        windowWidth -= event.getScreenX() - stage.getX();
                        stage.setWidth(windowWidth);
                        stage.setX(event.getScreenX());
                    }
                    break;
                }
                case NE: {
                    if (windowHeight > stage.getMinHeight() || event.getY() < 0) {
                        windowHeight -= event.getScreenY() - stage.getY();
                        stage.setHeight(windowHeight);
                        stage.setY(event.getScreenY());
                    }
                    if (windowWidth > stage.getMinWidth() || event.getX() - windowWidth > 0) {
                        windowWidth += event.getX() - windowWidth;
                        stage.setWidth(windowWidth);
                    }
                    break;
                }
                case NW: {
                    if (windowHeight > stage.getMinHeight() || event.getY() < 0) {
                        windowHeight -= event.getScreenY() - stage.getY();
                        stage.setHeight(windowHeight);
                        stage.setY(event.getScreenY());
                    }
                    if (windowWidth > stage.getMinWidth() || event.getX() < 0) {
                        windowWidth -= event.getScreenX() - stage.getX();
                        stage.setWidth(windowWidth);
                        stage.setX(event.getScreenX());
                    }
                    break;
                }
                case SE: {
                    if (windowHeight > stage.getMinHeight() || event.getY() - windowHeight > 0) {
                        windowHeight += event.getY() - windowHeight;
                        stage.setHeight(windowHeight);
                    }
                    if (windowWidth > stage.getMinWidth() || event.getX() - windowWidth > 0) {
                        windowWidth += event.getX() - windowWidth;
                        stage.setWidth(windowWidth);
                    }
                    break;
                }
                case SW: {
                    if (windowHeight > stage.getMinHeight() || event.getY() - windowHeight > 0) {
                        windowHeight += event.getY() - windowHeight;
                        stage.setHeight(windowHeight);
                    }
                    if (windowWidth > stage.getMinWidth() || event.getX() < 0) {
                        windowWidth -= event.getScreenX() - stage.getX();
                        stage.setWidth(windowWidth);
                        stage.setX(event.getScreenX());
                    }
                    break;
                }
                case MOVE: {
                    stage.setX(event.getScreenX() + deltaX);
                    stage.setY(event.getScreenY() + deltaY);
                }
            }
        });
    }
}
