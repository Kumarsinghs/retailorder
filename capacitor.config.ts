import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.meditrack.retailorder',
  appName: 'MediTrack Retail Order',
  webDir: 'www',
  bundledWebRuntime: false,

  // Load the live, already-deployed PWA directly inside the native WebView.
  // This guarantees 100% feature parity with the website (every screen,
  // form, WhatsApp/PDF export, offline banner, etc. behaves identically,
  // because it IS the same app — just running natively instead of in Chrome).
  server: {
    url: 'https://meditrack-retailorder.netlify.app/',
    cleartext: false,        // HTTPS only
    androidScheme: 'https',
    allowNavigation: [
      'meditrack-retailorder.netlify.app',
      '*.netlify.app'
    ]
  },

  android: {
    allowMixedContent: false,
    captureInput: true,
    webContentsDebuggingEnabled: false, // set true only for local debug builds
  },

  plugins: {
    SplashScreen: {
      launchShowDuration: 2000,
      launchAutoHide: true,
      backgroundColor: '#0A1628',
      androidSplashResourceName: 'splash',
      androidScaleType: 'CENTER_CROP',
      showSpinner: true,
      androidSpinnerStyle: 'large',
      spinnerColor: '#2DD4BF',
      splashFullScreen: true,
      splashImmersive: true,
    },
    StatusBar: {
      style: 'DARK',
      backgroundColor: '#0A1628',
      overlaysWebView: false,
    },
    Camera: {
      // No extra config needed; permissions requested at runtime.
    },
    Filesystem: {
      // Enables Capacitor's Filesystem API for saving PDFs/JSON exports
      // and reading uploaded prescription/invoice files.
    },
    App: {
      // Handles hardware back button + external link routing.
    },
    Keyboard: {
      resize: 'body',
      style: 'DARK',
      resizeOnFullScreen: true,
    },
  },
};

export default config;
