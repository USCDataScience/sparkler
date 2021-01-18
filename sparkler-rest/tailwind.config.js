const colors = require('tailwindcss/colors')

module.exports = {
  purge: [
      'src/main/resources/**/*.js',
     'src/main/resources/**/*.html',
     './src/**/*.html',
     './src/**/*.js',
],
  darkMode: false, // or 'media' or 'class'
  theme: {
     fontFamily: {
        sans: ['Nunito', 'sans-serif'],
        display: ['Nunito', 'sans-serif'],
        body: ['Nunito', 'sans-serif']
        },
      colors: {
          // Build your palette here
          transparent: 'transparent',
          current: 'currentColor',
          gray: colors.trueGray,
          red: colors.red,
          blue: colors.lightBlue,
          yellow: colors.amber,
          teal: colors.teal,
          emerald: colors.emerald
      },
    extend: {},
  },
  variants: {
    extend: {},
  },
  plugins: [
    require('tailwindcss'),
    require('autoprefixer'),
  ],
}
