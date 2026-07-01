// Add this near the top of your main entry file (e.g. main.jsx / main.tsx / index.js)
// or in a small <script> block right before </body> in index.html if the site is plain HTML/JS.

if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker
      .register('/sw.js')
      .then((reg) => console.log('MediTrack SW registered:', reg.scope))
      .catch((err) => console.error('MediTrack SW registration failed:', err));
  });
}
