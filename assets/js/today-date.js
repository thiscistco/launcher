(() => {
  const monthFormatter = new Intl.DateTimeFormat("id-ID", { month: "short" });

  function makeElement(tag, className, text) {
    const element = document.createElement(tag);
    element.className = className;
    element.textContent = text;
    return element;
  }

  function renderDate(panel, date) {
    const day = String(date.getDate());
    const month = monthFormatter.format(date).toLocaleUpperCase("id-ID");
    const year = String(date.getFullYear());

    panel.setAttribute("aria-label", `Tanggal ${day} ${month} ${year}`);

    const content = makeElement("div", "today-date-content", "");
    const meta = makeElement("div", "today-date-meta", "");
    content.append(makeElement("span", "today-date-day", day));
    meta.append(
      makeElement("span", "today-date-month", month),
      makeElement("span", "today-date-year", year)
    );
    content.append(meta);
    panel.replaceChildren(content);
  }

  function updateDates() {
    const now = new Date();
    document.querySelectorAll(".today-date-panel").forEach((panel) => renderDate(panel, now));
  }

  updateDates();
  window.setInterval(updateDates, 60_000);
})();
