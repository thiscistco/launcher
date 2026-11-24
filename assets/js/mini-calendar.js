(() => {
  const weekdayLabels = ["MIN", "SEN", "SEL", "RAB", "KAM", "JUM", "SAB"];
  const monthFormatter = new Intl.DateTimeFormat("id-ID", { month: "short" });

  function makeElement(tag, className, text) {
    const element = document.createElement(tag);
    element.className = className;
    if (text !== undefined) element.textContent = text;
    return element;
  }

  function renderCalendar(panel, date) {
    const year = date.getFullYear();
    const month = date.getMonth();
    const today = date.getDate();
    const firstWeekday = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const weekCount = Math.ceil((firstWeekday + daysInMonth) / 7);
    const monthName = monthFormatter.format(date).toLocaleUpperCase("id-ID");

    panel.setAttribute("aria-label", `Kalender ${monthName} ${year}`);
    panel.replaceChildren();

    const header = makeElement("div", "mini-calendar-header");
    header.append(
      makeElement("span", "mini-calendar-month", monthName),
      makeElement("span", "mini-calendar-year", String(year))
    );

    const weekdays = makeElement("div", "mini-calendar-weekdays");
    weekdays.setAttribute("aria-hidden", "true");
    weekdayLabels.forEach((label) => weekdays.append(makeElement("span", "", label)));

    const days = makeElement("div", "mini-calendar-days");
    days.style.setProperty("--week-count", weekCount);
    days.setAttribute("role", "grid");
    days.setAttribute("aria-label", `${monthName} ${year}`);

    for (let cell = 0; cell < weekCount * 7; cell += 1) {
      const dayNumber = cell - firstWeekday + 1;
      if (dayNumber < 1 || dayNumber > daysInMonth) {
        const emptyCell = makeElement("span", "mini-calendar-day empty");
        emptyCell.setAttribute("aria-hidden", "true");
        days.append(emptyCell);
        continue;
      }

      const weekday = cell % 7;
      const classes = ["mini-calendar-day"];
      if (weekday === 0 || weekday === 6) classes.push("weekend");
      if (dayNumber === today) classes.push("today");
      const dayCell = makeElement("span", classes.join(" "), String(dayNumber));
      dayCell.setAttribute("role", "gridcell");
      if (dayNumber === today) dayCell.setAttribute("aria-current", "date");
      days.append(dayCell);
    }

    panel.append(header, weekdays, days);
  }

  function updateCalendars() {
    const now = new Date();
    document.querySelectorAll(".date-calendar").forEach((panel) => renderCalendar(panel, now));
  }

  updateCalendars();
  window.setInterval(updateCalendars, 60_000);
})();
