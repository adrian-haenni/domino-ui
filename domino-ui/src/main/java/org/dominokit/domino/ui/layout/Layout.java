package org.dominokit.domino.ui.layout;

import elemental2.dom.*;
import org.dominokit.domino.ui.icons.BaseIcon;
import org.dominokit.domino.ui.mediaquery.MediaQuery;
import org.dominokit.domino.ui.style.ColorScheme;
import org.dominokit.domino.ui.style.Style;
import org.dominokit.domino.ui.themes.Theme;
import org.dominokit.domino.ui.utils.*;
import org.jboss.elemento.IsElement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static elemental2.dom.DomGlobal.document;
import static java.util.Objects.nonNull;
import static org.jboss.elemento.Elements.div;

public class Layout extends BaseDominoElement<HTMLDivElement, Layout> {

    private static final String SLIDE_OUT_LEFT = "slide-out-left";
    private static final String SLIDE_OUT_RIGHT = "slide-out-right";
    private static final String NONE = "none";
    private static final String BLOCK = "block";
    private static final String COLLAPSE = "collapse";
    private static final String CLICK = "click";
    public static final String FIT_WIDTH = "fit-width";
    public static final String FIT_HEIGHT = "fit-height";

    private DominoElement<HTMLDivElement> root = DominoElement.of(div().css("layout"));

    private final NavigationBar navigationBar = NavigationBar.create();
    private final Section section = Section.create();
    private final Overlay overlay = Overlay.create();
    private final Content content = Content.create();
    private final Footer footer = Footer.create();

    private Text appTitle = TextNode.empty();

    private boolean leftPanelVisible = false;
    private boolean rightPanelVisible = false;
    private boolean navigationBarExpanded = false;
    private boolean overlayVisible = false;
    private boolean leftPanelDisabled = false;
    private boolean footerVisible = false;
    private boolean fixedLeftPanel;
    private boolean fixedFooter = false;
    private LeftPanelSize leftPanelSize = LeftPanelSize.DEFAULT;

    private LayoutHandler onShowHandler = layout -> {
    };

    private LayoutHandler removeHandler = layout -> {
    };

    private List<Consumer<Boolean>> leftPanelHandlers = new ArrayList<>();

    public Layout() {
        this(null);
    }

    public Layout(String title) {
        init(this);
        if (nonNull(title)) {
            setTitle(title);
        }
        appendElements();
        initElementsPosition();
        addExpandListeners();
        if (!bodyStyle().contains("ls-hidden"))
            bodyStyle().add("ls-closed");
        new Theme(Theme.INDIGO).apply();

        DominoElement.of(document.body)
                .style()
                .add(leftPanelSize.getSize());

        MediaQuery.addOnSmallAndDownListener(() -> {
            if (footer.isAutoUnFixForSmallScreens() && footer.isFixed()) {
                fixedFooter = true;
                unfixFooter();
            }
        });

        MediaQuery.addOnSmallAndDownListener(() -> {
            if (footer.isAutoUnFixForSmallScreens() && isFooterVisible()) {
                fixedFooter = true;
                unfixFooter();
            }
        });

        MediaQuery.addOnMediumAndUpListener(() -> {
            if (footer.isAutoUnFixForSmallScreens() && nonNull(fixedFooter) && fixedFooter && isFooterVisible()) {
                fixFooter();
            }
        });

        if (nonNull(onShowHandler)) {
            onShowHandler.handleLayout(this);
        }
    }

    public static Layout create() {
        return new Layout();
    }

    public static Layout create(String title) {
        return new Layout(title);
    }

    public Layout show() {
        return show(ColorScheme.INDIGO, false);
    }

    public Layout show(boolean autoFixLeftPanel) {
        return show(ColorScheme.INDIGO, autoFixLeftPanel);
    }

    public Layout show(ColorScheme theme) {
        return show(theme, false);
    }

    public Layout show(ColorScheme theme, boolean autoFixLeftPanel) {
        new Theme(theme).apply();

        if (autoFixLeftPanel) {
            autoFixLeftPanel();
        }
        if (!root.isAttached()) {
            document.body.appendChild(root.element());
        }

        return this;
    }

    private void appendElements() {
        root.appendChild(overlay.element());
        root.appendChild(navigationBar.element());
        root.appendChild(section.element());
        root.appendChild(content.element());
        root.appendChild(footer.element());
        navigationBar.title.appendChild(appTitle);

        navigationBar.css("nav-fixed");
        navigationBar.style().remove("ls-closed");
    }

    public void remove(LayoutHandler removeHandler) {
        this.removeHandler = removeHandler;
        remove();
    }

    public Layout remove() {
        root.remove();
        removeHandler.handleLayout(this);
        return this;
    }

    private void initElementsPosition() {
        getLeftPanel()
                .style()
                .add(SLIDE_OUT_LEFT);

        getRightPanel()
                .style()
                .add(SLIDE_OUT_RIGHT);
    }

    private void addExpandListeners() {
        navigationBar.menu.addEventListener(CLICK, e -> toggleLeftPanel());
        overlay.addEventListener(CLICK, e -> hidePanels());
    }

    public Layout onShow(LayoutHandler layoutHandler) {
        this.onShowHandler = layoutHandler;
        return this;
    }

    public Layout removeLeftPanel() {
        return updateLeftPanel("none", "ls-closed", "ls-hidden");
    }

    public Layout addLeftPanel() {
        return updateLeftPanel("block", "ls-hidden", "ls-closed");
    }

    public Layout updateLeftPanel(String style, String hiddenStyle, String visibleStyle) {
        navigationBar.menu.style().setDisplay(style);
        getLeftPanel().style().setDisplay(style);
        bodyStyle().remove(hiddenStyle);
        bodyStyle().add(visibleStyle);

        return this;
    }

    private void hidePanels() {
        hideRightPanel();
        hideLeftPanel();
        hideOverlay();
    }

    public void toggleRightPanel() {
        if (rightPanelVisible)
            hideRightPanel();
        else
            showRightPanel();
    }

    public Layout showRightPanel() {
        if (leftPanelVisible)
            hideLeftPanel();
        getRightPanel().style().remove(SLIDE_OUT_RIGHT);
        rightPanelVisible = true;
        showOverlay();

        return this;
    }

    public Layout hideRightPanel() {
        getRightPanel().style().add(SLIDE_OUT_RIGHT);
        rightPanelVisible = false;
        hideOverlay();

        return this;
    }

    private void hideOverlay() {
        if (overlayVisible) {
            overlay.style().setDisplay("none");
            overlayVisible = false;
        }
    }

    private void showOverlay() {
        if (!overlayVisible) {
            overlay.style().setDisplay("block");
            overlayVisible = true;
        }
    }

    public Layout toggleLeftPanel() {
        if (leftPanelVisible)
            hideLeftPanel();
        else
            showLeftPanel();

        return this;
    }

    public Layout setLeftPanelSize(LeftPanelSize leftPanelSize) {
        DominoElement.of(document.body).style().remove(this.leftPanelSize.getSize());
        this.leftPanelSize = leftPanelSize;
        DominoElement.of(document.body).style().add(this.leftPanelSize.getSize());
        return this;
    }

    public Layout showLeftPanel() {
        if (!leftPanelDisabled) {
            if (rightPanelVisible)
                hideRightPanel();
            getLeftPanel().style().remove(SLIDE_OUT_LEFT);
            leftPanelVisible = true;
            showOverlay();
            DominoElement.of(document.body)
                    .styler(style -> style.add("panel-open"));
            leftPanelHandlers.forEach(handler -> handler.accept(true));
        }

        return this;
    }

    public Layout hideLeftPanel() {
        if (!fixedLeftPanel && !leftPanelDisabled) {
            getLeftPanel().style().add(SLIDE_OUT_LEFT);
            leftPanelVisible = false;
            hideOverlay();
            DominoElement.of(document.body)
                    .styler(style -> style.remove("panel-open"));
            leftPanelHandlers.forEach(handler -> handler.accept(false));
        }

        return this;
    }

    public boolean isFooterVisible() {
        return footerVisible;
    }

    public DominoElement<HTMLElement> getRightPanel() {
        return DominoElement.of(section.rightSide);
    }

    public DominoElement<HTMLElement> getLeftPanel() {
        return DominoElement.of(section.leftSide);
    }

    public DominoElement<HTMLDivElement> getContentPanel() {
        return DominoElement.of(content.contentContainer);
    }

    public DominoElement<HTMLUListElement> getTopBar() {
        return DominoElement.of(navigationBar.topBar);
    }

    public NavigationBar getNavigationBar() {
        return this.navigationBar;
    }

    public Content getContentSection() {
        return this.content;
    }

    public Footer getFooter() {
        return footer;
    }

    public Layout hideFooter() {
        footer.hide();
        return this;
    }

    public Layout showFooter() {
        footer.show();
        this.footerVisible = true;
        return this;
    }

    public Layout setTitle(String title) {
        if (navigationBar.title.hasChildNodes())
            navigationBar.title.removeChild(appTitle);
        this.appTitle = TextNode.of(title);
        navigationBar.title.appendChild(appTitle);

        return this;
    }

    public HTMLElement addActionItem(BaseIcon<?> icon) {
        return addActionItem(icon.element());
    }

    public HTMLElement addActionItem(IsElement element) {
        return addActionItem(element.element());
    }

    public HTMLElement addActionItem(HTMLElement element) {
        LayoutActionItem layoutActionItem = LayoutActionItem.create(element);
        getTopBar().appendChild(layoutActionItem);
        return layoutActionItem.element();
    }

    public Layout fixLeftPanelPosition() {
        if (!leftPanelDisabled) {
            showLeftPanel();
            hideOverlay();
            if (bodyStyle().contains("ls-closed"))
                bodyStyle().remove("ls-closed");
            this.fixedLeftPanel = true;
            DominoElement.body()
                    .style().add("l-fixed");
        }
        return this;
    }

    public Layout unfixLeftPanelPosition() {
        if (!leftPanelDisabled) {
            if (!bodyStyle().contains("ls-closed"))
                bodyStyle().add("ls-closed");
            this.fixedLeftPanel = false;
            DominoElement.body()
                    .style().remove("l-fixed");
        }
        return this;
    }

    public Layout spanLeftPanelUp(){
            DominoElement.body()
                    .style().add("l-panel-span-up");
            return this;
    }

    private Style<HTMLBodyElement, IsElement<HTMLBodyElement>> bodyStyle() {
        return Style.of(document.body);
    }

    public Layout disableLeftPanel() {
        unfixLeftPanelPosition();
        hideLeftPanel();
        getLeftPanel().style().setDisplay("none");
        navigationBar.menu.style()
                .remove("bars")
                .setDisplay("none");
        this.leftPanelDisabled = true;
        return this;
    }

    public Layout enableLeftPanel() {
        getLeftPanel().style().removeProperty("display");
        navigationBar.menu.style()
                .add("bars")
                .removeProperty("display");
        this.leftPanelDisabled = false;
        return this;
    }

    public Layout fixFooter() {
        footer.fixed();
        if (footer.isAttached()) {
            updateContentBottomPadding();
        } else {
            ElementUtil.onAttach(footer.element(), mutationRecord -> updateContentBottomPadding());
        }

        return this;
    }

    private void updateContentBottomPadding() {
        Style.of(content.element()).setPaddingBottom(footer.element().clientHeight + "px");
    }

    public Layout unfixFooter() {
        footer.unfixed();
        ElementUtil.onAttach(footer.element(), mutationRecord -> Style.of(content.element()).removeProperty("padding-bottom"));
        return this;
    }

    public Layout setHeaderHeight(String height) {
        navigationBar.style().setHeight(height);
        if (navigationBar.isAttached()) {
            updateContentMargin();
        } else {
            navigationBar.onAttached(mutationRecord -> {
                updateContentMargin();
            });
        }

        return this;
    }

    public Text getAppTitle() {
        return appTitle;
    }

    public Layout setContent(Node node) {
        getContentPanel()
                .clearElement()
                .appendChild(node);
        ElementUtil.scrollTop();
        return this;
    }

    public Layout setContent(IsElement element) {
        setContent(element.element());
        return this;
    }

    public Layout content(Consumer<DominoElement<HTMLDivElement>> contentConsumer) {
        contentConsumer.accept(getContentPanel());
        return this;
    }

    public Layout leftPanel(Consumer<DominoElement<HTMLElement>> leftPanelConsumer) {
        leftPanelConsumer.accept(getLeftPanel());
        return this;
    }

    public Layout rightPanel(Consumer<DominoElement<HTMLElement>> rightPanelConsumer) {
        rightPanelConsumer.accept(getRightPanel());
        return this;
    }

    public Layout footer(Consumer<Footer> footerConsumer) {
        footerConsumer.accept(getFooter());
        return this;
    }

    public Layout navigationBar(Consumer<NavigationBar> navigationBarConsumer) {
        navigationBarConsumer.accept(getNavigationBar());
        return this;
    }

    public Layout topBar(Consumer<DominoElement<HTMLUListElement>> topBarConsumer) {
        topBarConsumer.accept(getTopBar());
        return this;
    }

    public Layout setLogo(HTMLImageElement imageElement) {
        navigationBar
                .getLogoItem()
                .clearElement()
                .appendChild(imageElement)
                .css("logo-in");
        return this;
    }

    public Layout setLogo(IsElement<HTMLImageElement> imageElement) {
        return setLogo(imageElement.element());
    }

    public Layout autoFixLeftPanel() {
        MediaQuery.addOnMediumAndUpListener(() -> {
            if (Layout.this.getLeftPanel().isAttached()) {
                Layout.this.fixLeftPanelPosition();
            } else {
                Layout.this.getLeftPanel().onAttached(mutationRecord -> Layout.this.fixLeftPanelPosition());
            }

        });
        MediaQuery.addOnSmallAndDownListener(() -> {
            if (getLeftPanel().isAttached()) {
                this.unfixLeftPanelPosition();
                this.hideLeftPanel();
            } else {
                this.unfixLeftPanelPosition();
                this.hideLeftPanel();
            }
        });

        return this;
    }

    public Layout hideNavBarExpand() {
        navigationBar.getMenuToggleItem().hide();
        return this;
    }

    public Layout hideNavBarExpandOnSmallDown() {
        return hideNavBarExpandOn(ScreenMedia.SMALL_AND_DOWN);
    }

    public Layout hideNavBarExpandOn(ScreenMedia screenMedia) {
        navigationBar.getMenuToggleItem().hideOn(screenMedia);
        return this;
    }

    public Layout onLeftPanelStateChanged(Consumer<Boolean> leftPanelHandler) {
        leftPanelHandlers.add(leftPanelHandler);
        return this;
    }

    public Layout removeLeftPanelHandler(Consumer<Boolean> leftPanelHandler) {
        leftPanelHandlers.remove(leftPanelHandler);
        return this;
    }

    public Layout fitWidth() {
        content.styler(style -> style.add(FIT_WIDTH));
        getContentPanel().styler(style -> style.add(FIT_WIDTH));
        return this;
    }

    public Layout unfitWidth() {
        content.styler(style -> style.remove(FIT_WIDTH));
        getContentPanel().styler(style -> style.remove(FIT_WIDTH));
        return this;
    }

    public Layout fitHeight() {
        content.styler(style -> style.add(FIT_HEIGHT));
        getFooter().styler(style -> style.add(FIT_HEIGHT));
        return this;
    }

    public Layout unfitHeight() {
        content.styler(style -> style.remove(FIT_HEIGHT));
        getFooter().styler(style -> style.remove(FIT_HEIGHT));
        return this;
    }

    private void updateContentMargin() {
        double margin = navigationBar.getBoundingClientRect().height + 30;
        content.style().setMarginTop(margin + "px");
    }

    public boolean isLeftPanelVisible() {
        return leftPanelVisible;
    }

    public boolean isRightPanelVisible() {
        return rightPanelVisible;
    }

    public boolean isNavigationBarExpanded() {
        return navigationBarExpanded;
    }

    public boolean isOverlayVisible() {
        return overlayVisible;
    }

    public boolean isLeftPanelDisabled() {
        return leftPanelDisabled;
    }

    public boolean isFixedLeftPanel() {
        return fixedLeftPanel;
    }

    public LeftPanelSize getLeftPanelSize() {
        return leftPanelSize;
    }

    @Override
    public HTMLDivElement element() {
        return root.element();
    }

    public enum LeftPanelSize {
        SMALL("sm"),
        DEFAULT("md"),
        LARGE("lg");

        private String size;

        LeftPanelSize(String size) {
            this.size = size;
        }

        public String getSize() {
            return size;
        }
    }

    @FunctionalInterface
    public interface LayoutHandler {
        void handleLayout(Layout layout);
    }
}
