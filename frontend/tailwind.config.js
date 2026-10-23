export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: { extend: {
    colors: { bg: 'var(--bg)', card: 'var(--card)', ink: 'var(--ink)', mut: 'var(--mut)', line: 'var(--line)', pop: 'var(--pop)', brand: 'var(--brand)', onbrand: 'var(--onbrand)', ok: 'var(--ok)', bad: 'var(--bad)' },
    fontFamily: { display: ['"Bricolage Grotesque"', 'system-ui', 'sans-serif'], sans: ['"Hanken Grotesk"', 'system-ui', 'sans-serif'] },
  } },
};
