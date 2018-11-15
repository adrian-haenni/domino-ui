package org.dominokit.domino.ui.datepicker;

import elemental2.core.JsDate;
import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLDivElement;
import org.dominokit.domino.ui.button.Button;
import org.dominokit.domino.ui.forms.Select;
import org.dominokit.domino.ui.forms.SelectOption;
import org.dominokit.domino.ui.grid.Column;
import org.dominokit.domino.ui.grid.Row;
import org.dominokit.domino.ui.icons.Icons;
import org.dominokit.domino.ui.modals.ModalDialog;
import org.dominokit.domino.ui.pickers.PickerHandler;
import org.dominokit.domino.ui.style.ColorScheme;
import org.dominokit.domino.ui.style.WavesSupport;
import org.dominokit.domino.ui.utils.BaseDominoElement;
import org.dominokit.domino.ui.utils.DominoElement;
import org.dominokit.domino.ui.utils.HasValue;
import org.gwtproject.editor.client.LeafValueEditor;
import org.gwtproject.i18n.client.impl.cldr.DateTimeFormatInfo_factory;
import org.gwtproject.i18n.shared.DateTimeFormat;
import org.gwtproject.i18n.shared.DateTimeFormatInfo;
import org.jboss.gwt.elemento.core.EventType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.Objects.nonNull;
import static org.jboss.gwt.elemento.core.Elements.a;
import static org.jboss.gwt.elemento.core.Elements.div;

public class DatePicker extends BaseDominoElement<HTMLDivElement, DatePicker> implements HasValue<DatePicker, Date>,
        DatePickerMonth.InternalHandler, LeafValueEditor<Date> {

    private final JsDate jsDate;
    private DominoElement<HTMLDivElement> element = DominoElement.of(div().css("calendar"));
    private DominoElement<HTMLDivElement> headerPanel = DominoElement.of(div().css("date-panel"));
    private DominoElement<HTMLDivElement> selectorsPanel = DominoElement.of(div().css("selector-container"));
    private DominoElement<HTMLDivElement> footerPanel = DominoElement.of(div().css("cal-footer"));

    private DominoElement<HTMLDivElement> dayName = DominoElement.of(div().css("day-name"));
    private DominoElement<HTMLDivElement> monthName = DominoElement.of(div().css("month-name"));
    private DominoElement<HTMLDivElement> dateNumber = DominoElement.of(div().css("day-number"));
    private DominoElement<HTMLDivElement> yearNumber = DominoElement.of(div().css("year-number"));
    private DominoElement<HTMLAnchorElement> navigateBefore;
    private DominoElement<HTMLAnchorElement> navigateNext;

    private Select<Integer> yearSelect;
    private Select<Integer> monthSelect;
    private Button todayButton;
    private Button clearButton;
    private Button closeButton;

    private ColorScheme colorScheme = ColorScheme.LIGHT_BLUE;

    private final DatePickerMonth datePickerMonth;
    private DatePickerElement selectedPickerElement;

    private List<PickerHandler> closeHandlers = new ArrayList<>();
    private List<PickerHandler> clearHandlers = new ArrayList<>();
    private BackgroundHandler backgroundHandler = (oldBackground, newBackground) -> {
    };

    private Column column = Column.span(4);

    private JsDate minDate;
    private JsDate maxDate;

    private List<DateSelectionHandler> dateSelectionHandlers = new ArrayList<>();
    private List<DateDayClickedHandler> dateDayClickedHandlers = new ArrayList<>();


    public DatePicker(Date date, DateTimeFormatInfo dateTimeFormatInfo) {

        this.jsDate = new JsDate((double) date.getTime());

        this.minDate = new JsDate((double) date.getTime());
        minDate.setFullYear(minDate.getFullYear() - 50);

        this.maxDate = new JsDate((double) date.getTime());
        maxDate.setFullYear(maxDate.getFullYear() + 50);

        datePickerMonth = DatePickerMonth.create(this.jsDate, dateTimeFormatInfo, this);
        build();
    }

    public DatePicker(Date date, DateTimeFormatInfo dateTimeFormatInfo, Date minDate, Date maxDate) {
        this.jsDate = new JsDate((double) date.getTime());
        this.minDate = new JsDate((double) minDate.getTime());
        this.maxDate = new JsDate((double) maxDate.getTime());
        datePickerMonth = DatePickerMonth.create(this.jsDate, dateTimeFormatInfo, this);
        build();
    }

    private DatePicker(JsDate date, DateTimeFormatInfo dateTimeFormatInfo, JsDate minDate, JsDate maxDate) {
        this.jsDate = date;
        this.minDate = minDate;
        this.maxDate = maxDate;
        datePickerMonth = DatePickerMonth.create(this.jsDate, dateTimeFormatInfo, this);
        build();
    }

    private void build() {

        element.appendChild(headerPanel);
        headerPanel.style().add(colorScheme.color().getBackground());
        dayName.style().add(colorScheme.darker_2().getBackground());
        headerPanel.appendChild(dayName);
        headerPanel.appendChild(monthName);
        headerPanel.appendChild(dateNumber);
        headerPanel.appendChild(yearNumber);

        element.appendChild(selectorsPanel);
        initSelectors();
        element.appendChild(datePickerMonth);
        element.appendChild(footerPanel);
        initFooter();

        datePickerMonth.init();
    }


    private void initFooter() {

        clearButton = Button.create("CLEAR").setColor(colorScheme.color());
        clearButton.style().add("clear-button");

        clearButton.addClickListener(evt -> {
            clearHandlers.forEach(PickerHandler::handle);
        });

        todayButton = Button.create("TODAY").setColor(colorScheme.color());
        todayButton.addClickListener(evt -> setDate(new Date()));

        closeButton = Button.create("CLOSE").setColor(colorScheme.color());
        closeButton.style().add("close-button");

        closeButton.addClickListener(evt -> {
            closeHandlers.forEach(PickerHandler::handle);
        });

        footerPanel.appendChild(clearButton.linkify());
        footerPanel.appendChild(todayButton.linkify());
        footerPanel.appendChild(closeButton.linkify());

    }

    private void initSelectors() {

        int year = jsDate.getFullYear();
        yearSelect = Select.create();
        yearSelect.style().setMarginBottom("0px", true);

        for (int i = minDate.getFullYear(); i <= maxDate.getFullYear(); i++) {
            SelectOption<Integer> yearOption = SelectOption.create(i, i + "");
            yearSelect.appendChild(yearOption);

            if (i == year)
                yearSelect.select(yearOption);
        }
        yearSelect.addSelectionHandler(option -> {
            int selectedYear = option.getValue();
            jsDate.setYear(selectedYear);
            setDate(jsDate);
        });

        int month = jsDate.getMonth();
        monthSelect = Select.create();
        monthSelect.style().setMarginBottom("0px", true);
        String[] months = getDateTimeFormatInfo().monthsShort();
        for (int i = 0; i < months.length; i++) {
            SelectOption<Integer> monthOption = SelectOption.create(i, months[i]);
            monthSelect.appendChild(monthOption);
            if (i == month)
                monthSelect.select(monthOption);
        }
        monthSelect.addSelectionHandler(option -> {
            int selectedMonth = option.getValue();
            jsDate.setMonth(selectedMonth);
            setDate(jsDate);
        });

        Column yearColumn = this.column.copy().appendChild(yearSelect.asElement());
        yearColumn.style().setPaddingLeft("0px", true);
        yearColumn.style().setPaddingRight("3px", true);
        yearColumn.style().setMarginBottom("5px", true);

        Column monthColumn = this.column.copy().appendChild(monthSelect.asElement());
        monthColumn.style().setPaddingLeft("3px", true);
        monthColumn.style().setPaddingRight("0px", true);
        monthColumn.style().setMarginBottom("5px", true);

        Column backColumn = Column.span(2);

        backColumn.style().setPadding("0px", true);
        backColumn.style().setMarginBottom("5px", true);

        Column forwardColumn = Column.span(2);
        forwardColumn.style().setPadding("0px", true);
        forwardColumn.style().setTextAlign("right", true);
        forwardColumn.style().setMarginBottom("5px", true);


        Row row = Row.create();
        row.style().setMarginLeft("0px", true);
        row.style().setMarginRight("0px", true);
        navigateBefore = DominoElement.of(a().css("navigate").add(Icons.ALL.navigate_before()));
        navigateNext = DominoElement.of(a().css("navigate").add(Icons.ALL.navigate_next()));

        WavesSupport.addFor(navigateBefore);
        WavesSupport.addFor(navigateNext);

        navigateBefore.addEventListener(EventType.click.getName(), evt -> {
            JsDate jsDate = getJsDate();
            int currentMonth = jsDate.getMonth();
            if (currentMonth == 0) {
                jsDate.setYear(jsDate.getFullYear() - 1);
                jsDate.setMonth(11);
            } else {
                jsDate.setMonth(currentMonth - 1);
            }

            yearSelect.setValue(jsDate.getFullYear(), true);
            monthSelect.selectAt(jsDate.getMonth(), true);
            setDate(jsDate);
        });


        navigateNext.addEventListener(EventType.click.getName(), evt -> {
            JsDate jsDate = getJsDate();
            int currentMonth = jsDate.getMonth();
            if (currentMonth == 11) {
                jsDate.setYear(jsDate.getFullYear() + 1);
                jsDate.setMonth(0);
            } else {
                jsDate.setMonth(currentMonth + 1);
            }

            yearSelect.setValue(jsDate.getFullYear(), true);
            monthSelect.selectAt(jsDate.getMonth(), true);
            setDate(jsDate);
        });


        selectorsPanel.appendChild(row
                .appendChild(backColumn.appendChild(navigateBefore))
                .appendChild(yearColumn)
                .appendChild(monthColumn)
                .appendChild(forwardColumn.appendChild(navigateNext)));
    }


    public static DatePicker create() {
        DateTimeFormatInfo dateTimeFormatInfo = DateTimeFormatInfo_factory.create();

        return new DatePicker(new Date(), dateTimeFormatInfo);
    }

    public static DatePicker create(Date date) {
        DateTimeFormatInfo dateTimeFormatInfo = DateTimeFormatInfo_factory.create();
        return new DatePicker(date, dateTimeFormatInfo);
    }

    public static DatePicker create(Date date, DateTimeFormatInfo dateTimeFormatInfo) {
        return new DatePicker(date, dateTimeFormatInfo);
    }

    public static DatePicker create(Date date, DateTimeFormatInfo dateTimeFormatInfo, Date minDate, Date maxDate) {
        return new DatePicker(date, dateTimeFormatInfo, minDate, maxDate);
    }

    @Override
    public HTMLDivElement asElement() {
        return element.asElement();
    }

    @Override
    public DatePicker value(Date value) {
        setValue(value);
        return this;
    }

    @Override
    public Date getValue() {
        return datePickerMonth.getValue();
    }

    @Override
    public void setValue(Date value) {
        datePickerMonth.value(value);
    }

    public DatePicker setDate(Date date) {
        this.value(date);
        return this;
    }

    public Date getDate() {
        return this.getValue();
    }

    public DatePicker setDate(JsDate jsDate) {
        this.value(new Date(new Double(jsDate.getTime()).longValue()));
        return this;
    }

    public JsDate getJsDate() {
        return new JsDate((double) getValue().getTime());
    }

    public DatePicker addDateSelectionHandler(DateSelectionHandler dateSelectionHandler) {
        this.dateSelectionHandlers.add(dateSelectionHandler);
        return this;
    }

    public DatePicker removeDateSelectionHandler(DateSelectionHandler dateSelectionHandler) {
        this.dateSelectionHandlers.remove(dateSelectionHandler);
        return this;
    }

    public List<DateSelectionHandler> getDateSelectionHandlers() {
        return this.dateSelectionHandlers;
    }

    public DatePicker clearDaySelectionHandlers() {
        this.dateSelectionHandlers.clear();
        return this;
    }


    public DatePicker addDateDayClickHandler(DateDayClickedHandler dateDayClickedHandler) {
        this.dateDayClickedHandlers.add(dateDayClickedHandler);
        return this;
    }

    public DatePicker removeDateDayClickedHandler(DateDayClickedHandler dateClickedHandler) {
        this.dateDayClickedHandlers.remove(dateClickedHandler);
        return this;
    }

    public List<DateDayClickedHandler> getDateDayClickedHandlers() {
        return this.dateDayClickedHandlers;
    }

    public DatePicker clearDateDayClickedHandlers() {
        this.dateDayClickedHandlers.clear();
        return this;
    }

    public DatePicker setDateTimeFormatInfo(DateTimeFormatInfo dateTimeFormatInfo) {
        this.datePickerMonth.setDateTimeFormatInfo(dateTimeFormatInfo);
        updatePicker();
        return this;
    }

    public DateTimeFormatInfo getDateTimeFormatInfo() {
        return datePickerMonth.getDateTimeFormatInfo();
    }

    public DatePicker showBorder() {
        element.style().setBorder("1px solid " + colorScheme.color().getHex());
        return this;
    }

    public DatePicker setColorScheme(ColorScheme colorScheme) {
        backgroundHandler.onBackgroundChanged(getColorScheme(), colorScheme);
        this.headerPanel.style().remove(this.colorScheme.color().getBackground());
        this.dayName.style().remove(this.colorScheme.darker_2().getBackground());
        this.colorScheme = colorScheme;
        this.headerPanel.style().add(this.colorScheme.color().getBackground());
        this.dayName.style().add(this.colorScheme.darker_2().getBackground());
        this.datePickerMonth.setBackground(colorScheme.color());
        this.todayButton.setColor(colorScheme.color());
        this.closeButton.setColor(colorScheme.color());
        this.clearButton.setColor(colorScheme.color());
        return this;
    }

    public ColorScheme getColorScheme() {
        return colorScheme;
    }

    @Override
    public void onDaySelected(DatePickerElement datePickerElement) {
        this.selectedPickerElement = datePickerElement;
        updatePicker();
        publish();
    }

    @Override
    public void onDayClicked(DatePickerElement datePickerElement) {
        for (int i = 0; i < dateDayClickedHandlers.size(); i++) {
            dateDayClickedHandlers.get(i).onDateDayClicked(getDate(), getDateTimeFormatInfo());
        }
    }

    private void publish() {
        for (int i = 0; i < dateSelectionHandlers.size(); i++) {
            dateSelectionHandlers.get(i).onDateSelected(getDate(), getDateTimeFormatInfo());
        }
    }

    private void updatePicker() {
        int dayNameIndex = this.selectedPickerElement.getWeekDay() + getDateTimeFormatInfo().firstDayOfTheWeek();
        if (dayNameIndex > 6) {
            dayNameIndex = this.selectedPickerElement.getWeekDay() + getDateTimeFormatInfo().firstDayOfTheWeek() - 7;
        }
        this.dayName.setTextContent(getDateTimeFormatInfo().weekdaysFull()[dayNameIndex]);
        this.monthName.setTextContent(getDateTimeFormatInfo().monthsFull()[this.selectedPickerElement.getMonth()]);
        this.dateNumber.setTextContent(this.selectedPickerElement.getDay() + "");
        this.yearNumber.setTextContent(this.selectedPickerElement.getYear() + "");
        this.monthSelect.selectAt(this.selectedPickerElement.getMonth(), true);
        this.yearSelect.setValue(this.selectedPickerElement.getYear(), true);
    }


    public DatePicker showHeaderPanel() {
        headerPanel.style().setDisplay("block");
        return this;
    }

    public DatePicker hideHeaderPanel() {
        headerPanel.style().setDisplay("none");
        return this;
    }

    public DatePicker showTodayButton() {
        this.todayButton.style().setDisplay("block");
        return this;
    }

    public DatePicker hideTodayButton() {
        this.todayButton.style().setDisplay("none");
        return this;
    }

    public DatePicker showClearButton() {
        this.clearButton.style().setDisplay("block");
        return this;
    }

    public DatePicker hideClearButton() {
        this.clearButton.style().setDisplay("none");
        return this;
    }


    public DatePicker showCloseButton() {
        this.closeButton.style().setDisplay("block");
        return this;
    }

    public DatePicker hideCloseButton() {
        this.closeButton.style().setDisplay("none");
        return this;
    }

    public DatePicker addCloseHandler(PickerHandler closeHandler) {
        this.closeHandlers.add(closeHandler);
        return this;
    }

    public DatePicker removeCloseHandler(PickerHandler closeHandler) {
        this.closeHandlers.remove(closeHandler);
        return this;
    }

    public List<PickerHandler> getCloseHandlers() {
        return this.closeHandlers;
    }

    public DatePicker addClearHandler(PickerHandler clearHandler) {
        this.clearHandlers.add(clearHandler);
        return this;
    }

    public DatePicker removeClearHandler(PickerHandler clearHandler) {
        this.clearHandlers.remove(clearHandler);
        return this;
    }

    public List<PickerHandler> getClearHandlers() {
        return this.clearHandlers;
    }

    public DatePicker todayButtonText(String text) {
        this.todayButton.setContent(text);
        this.todayButton.asElement().title = text;
        return this;
    }

    public DatePicker clearButtonText(String text) {
        this.clearButton.setContent(text);
        this.clearButton.asElement().title = text;
        return this;
    }

    public DatePicker closeButtonText(String text) {
        this.closeButton.setContent(text);
        this.closeButton.asElement().title = text;
        return this;
    }

    public DatePicker fixedWidth() {
        element.style().setWidth("300px", true);
        return this;
    }

    public DatePicker fixedWidth(String width) {
        element.style().setWidth(width, true);
        return this;
    }

    public DominoElement<HTMLDivElement> getHeaderPanel() {
        return DominoElement.of(headerPanel);
    }

    public DominoElement<HTMLDivElement> getSelectorsPanel() {
        return DominoElement.of(selectorsPanel);
    }

    public DominoElement<HTMLDivElement> getFooterPanel() {
        return DominoElement.of(footerPanel);
    }

    public DominoElement<HTMLDivElement> getDayNamePanel() {
        return DominoElement.of(dayName);
    }

    public DominoElement<HTMLDivElement> getMonthNamePanel() {
        return DominoElement.of(monthName);
    }

    public DominoElement<HTMLDivElement> getDateNumberPanel() {
        return DominoElement.of(dateNumber);
    }

    public DominoElement<HTMLDivElement> getYearNumberPanel() {
        return DominoElement.of(yearNumber);
    }

    public DominoElement<HTMLAnchorElement> getNavigateBefore() {
        return DominoElement.of(navigateBefore);
    }

    public DominoElement<HTMLAnchorElement> getNavigateNext() {
        return DominoElement.of(navigateNext);
    }

    DatePicker setBackgroundHandler(BackgroundHandler backgroundHandler) {
        if (nonNull(backgroundHandler)) {
            this.backgroundHandler = backgroundHandler;
        }
        return this;
    }

    public ModalDialog createModal(String title) {
        return ModalDialog.createPickerModal(title, this.getColorScheme(), this.asElement());
    }

    @FunctionalInterface
    interface BackgroundHandler {
        void onBackgroundChanged(ColorScheme oldColorScheme, ColorScheme newColorScheme);
    }

    @FunctionalInterface
    public interface DateSelectionHandler {
        void onDateSelected(Date date, DateTimeFormatInfo dateTimeFormatInfo);
    }

    @FunctionalInterface
    public interface DateDayClickedHandler {
        void onDateDayClicked(Date date, DateTimeFormatInfo dateTimeFormatInfo);
    }

    public static class Formatter extends DateTimeFormat {

        protected Formatter(String pattern) {
            super(pattern);
        }

        protected Formatter(String pattern, DateTimeFormatInfo dtfi) {
            super(pattern, dtfi);
        }

        public static DateTimeFormat getFormat(String pattern, DateTimeFormatInfo dateTimeFormatInfo) {
            return DateTimeFormat.getFormat(pattern, dateTimeFormatInfo);
        }
    }
}
