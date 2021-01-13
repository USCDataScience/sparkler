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
