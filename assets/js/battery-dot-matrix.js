(() => {
  const glyphs = {
    "0": ["111", "101", "101", "101", "111"],
    "1": ["010", "110", "010", "010", "111"],
    "2": ["111", "001", "111", "100", "111"],
    "3": ["111", "001", "111", "001", "111"],
    "4": ["101", "101", "111", "001", "001"],
    "5": ["111", "100", "111", "001", "111"],
    "6": ["111", "100", "111", "101", "111"],
    "7": ["111", "001", "010", "010", "010"],
    "8": ["111", "101", "111", "101", "111"],
    "9": ["111", "101", "111", "001", "111"],
    "%": ["101", "001", "010", "100", "101"]
  };
  const blankGlyph = ["000", "000", "000", "000", "000"];
  const labelWidth = 22.6;
  const labelHeight = 12.7;

  document.querySelectorAll(".vent-battery[data-battery]").forEach((indicator) => {
    const canvas = document.createElement("canvas");
    const pixelRatio = Math.min(window.devicePixelRatio || 1, 4);
    canvas.className = "battery-readout";
    canvas.width = Math.round(labelWidth * pixelRatio);
    canvas.height = Math.round(labelHeight * pixelRatio);
    canvas.setAttribute("role", "img");
    canvas.setAttribute("aria-label", `Battery ${indicator.dataset.battery}`);

    const context = canvas.getContext("2d");
    context.scale(pixelRatio, pixelRatio);

    const chars = [...indicator.dataset.battery];
    const dotSize = Math.min(labelHeight / 10, labelWidth / 30) * 0.95;
    const dotGap = dotSize * 0.5;
    const characterGap = dotSize * 0.8;
    const glyphWidth = dotSize * 3 + dotGap * 2;
    const textWidth = glyphWidth * chars.length + characterGap * (chars.length - 1);
    const textHeight = dotSize * 5 + dotGap * 4;
    const startX = (labelWidth - textWidth) / 2;
    const startY = (labelHeight - textHeight) / 2;

    chars.forEach((char, characterIndex) => {
      const glyph = glyphs[char] || blankGlyph;
      glyph.forEach((row, rowIndex) => {
        [...row].forEach((dot, columnIndex) => {
          if (dot !== "1") return;
          const x = startX + characterIndex * (glyphWidth + characterGap) +
            columnIndex * (dotSize + dotGap) + dotSize / 2;
          const y = startY + rowIndex * (dotSize + dotGap) + dotSize / 2;

          context.beginPath();
          context.fillStyle = "rgba(255, 106, 0, 0.22)";
          context.arc(x, y, dotSize * 0.85, 0, Math.PI * 2);
          context.fill();

          context.beginPath();
          context.fillStyle = "#FF6A00";
          context.arc(x, y, dotSize * 0.42, 0, Math.PI * 2);
          context.fill();
        });
      });
    });

    indicator.appendChild(canvas);
  });
})();
