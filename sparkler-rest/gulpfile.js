const gulp = require('gulp');
const babel = require("gulp-babel");
const watch = require('gulp-watch');
const browserSync = require('browser-sync').create();
const environments = require('gulp-environments');
const uglifycss = require('gulp-uglifycss');
const terser = require('gulp-terser');
var browserify = require('browserify');
var sourceMaps = require('gulp-sourcemaps');
var coffeeify  = require('coffeeify');
var babelify   = require('babelify')
var livereload = require('gulp-livereload');
var source     = require('vinyl-source-stream');
var buffer = require('vinyl-buffer');
var rename = require('gulp-rename')
var watchify = require('watchify');
var postcss = require('gulp-postcss');
var cssvars = require('postcss-simple-vars');
var nested = require('postcss-nested');
var cssImport = require('postcss-import');
var autoprefixer = require('autoprefixer');
var tailwindcss = require('tailwindcss');


const production = environments.production;
var config = {
    js: {
        src: './src/main/resources/static/js/application.js',       // Entry point
        outputDir: './target/classes/static/js/',  // Directory to save bundle to
        mapDir: './maps/',      // Subdirectory to save maps to
        outputFile: 'bundle.js' // Name to use for bundle
    },
};

function browser(done){

}

gulp.task('javascript', function (done) {
    let bundler = watchify(browserify(config.js.src)  // Pass browserify the entry point
        .transform(coffeeify)      //  Chain transformations: First, coffeeify . . .
        .transform(babelify, { presets : [ "@babel/preset-env" ], plugins :[ "@babel/plugin-proposal-class-properties"] }))

    bundler
        .bundle()                                                        // Start bundle
        .pipe(source(config.js.src))                        // Entry point
        .pipe(buffer())                                               // Convert to gulp pipeline
        .pipe(rename(config.js.outputFile))          // Rename output from 'main.js'
        .pipe(gulp.dest(config.js.outputDir))
        .pipe(livereload()).on('end', function() {
            console.log("ended");
        done();
    });
    //.pipe(sourceMaps.init({ loadMaps : true }))  // Strip inline source maps
    //.pipe(sourceMaps.write(config.js.mapDir))    // Save source maps to their
});

gulp.task('javascript-build', function (done) {
    let bundler = browserify(config.js.src)  // Pass browserify the entry point
        .transform(coffeeify)      //  Chain transformations: First, coffeeify . . .
        .transform(babelify, { presets : [ "@babel/preset-env" ], plugins :[ "@babel/plugin-proposal-class-properties"] })

    bundler
        .bundle()                                                        // Start bundle
        .pipe(source(config.js.src))                        // Entry point
        .pipe(buffer())                                               // Convert to gulp pipeline
        .pipe(rename(config.js.outputFile))          // Rename output from 'main.js'
        .pipe(gulp.dest(config.js.outputDir))
        .pipe(livereload()).on('end', function() {
        console.log("ended");
        done();
    });
    //.pipe(sourceMaps.init({ loadMaps : true }))  // Strip inline source maps
    //.pipe(sourceMaps.write(config.js.mapDir))    // Save source maps to their
});
gulp.task('watch', () => {
    browserSync.init({
        proxy: 'localhost:8080',
        snippetOptions: {
            rule: {
                match: /<\/head>/i,
                fn: function (snippet, match) {
                    return snippet + match;
                }
            }
        },
    });

    gulp.watch(['src/main/resources/**/*.html'], gulp.series('copy-html-and-reload'));
    gulp.watch(['src/main/resources/**/*.css'], gulp.series('copy-css-and-reload'));
    gulp.watch(['src/main/resources/**/*.js'], gulp.series('copy-js-and-reload'));
});

gulp.task('copy-html', () =>
    gulp.src(['src/main/resources/**/*.html'])
        .pipe(gulp.dest('target/classes/'))
);

gulp.task('copy-css', () =>
    gulp.src(['src/main/resources/**/*.css'])
        .pipe(production(uglifycss()))
        .pipe(gulp.dest('target/classes/'))
);

gulp.task('copy-js', () =>
    gulp.src(['src/main/resources/**/*.js'])
        .pipe(babel())
        .pipe(production(terser()))
        .pipe(gulp.dest('target/classes/'))
);

gulp.task('styles', function(){
 return gulp.src('src/main/resources/static/css/main.css')
  .pipe(postcss([tailwindcss, cssImport, cssvars, nested, autoprefixer]))
  .pipe(gulp.dest('target/classes/static/css/'))
});

gulp.task('copy-html-and-reload', gulp.series('copy-html', reload));
gulp.task('copy-css-and-reload', gulp.series('styles', reload));
gulp.task('copy-js-and-reload', gulp.series('javascript', reload));

gulp.task('build', gulp.series('copy-html', 'styles', 'javascript-build'));
gulp.task('default', gulp.series('copy-html', 'styles', 'javascript-build', 'watch'));

function reload(done) {
    browserSync.reload();
    done();
}
