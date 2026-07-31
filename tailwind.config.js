/** @type {import('tailwindcss').Config} */
const colors = require('tailwindcss/colors')

module.exports = {
  content: ["./src/**/*.{js,jsx,ts,tsx}"],
  presets: [require("nativewind/preset")],
  theme: {
    extend: {
      colors: {
        page: '#000000',
        border: colors.slate[50],
        'text-primary': colors.slate[50],
        'text-secondary': colors.slate[400],
        primary: colors.slate[600],
        error: colors.red[500],
      }
    },
  },
  plugins: [],
}

