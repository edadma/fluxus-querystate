module.exports = {
  // Content sources - use glob patterns for better file matching
  content: [
    './examples/**/*.{scala,js,html}',
    './index.html',
  ],

  // Tailwind v4 recommends using the new `content` configuration
  // which replaces the older content detection method

  // Theme customization
  theme: {
    extend: {
      // Add custom theme extensions if needed
      fontFamily: {
        // Example of custom font family
        // 'custom': ['YourCustomFont', 'sans-serif']
      },
      colors: {
        // Add custom color definitions if required
      }
    }
  },

  // Plugins
  plugins: [
    require('@tailwindcss/typography'),
    require('daisyui')
  ],

  // DaisyUI themes (if using)
  daisyui: {
    themes: ["light", "dark", "night", "cupcake"],
  },

  // New in v4: Optional performance and output configurations
  performance: {
    // Optimize for speed or size
    preset: 'default'
  },

  // Experimental features (optional)
  experimental: {
    optimizeUniversalDefaults: true
  }
}
