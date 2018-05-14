---
layout: page
title: "Contributing to Docs"
category: dev
date: 2017-12-26 15:27:29
---

Contributions are welcome all the way - big or small, including adding tutorials and how-to's!

This page helps how to update documentation.

To add a new page to this website

```bash
ruby bin/jekyll-page "Page Title" <category>
```

`<category>` can be:

- `doc` - Documentation
- `tut` - Tutorial
- `ref` - Reference
- `dev` -  Developers
- `post` -  Posts

For example, if you want to write a tutorial about **Crawling images using Sparkler**



```bash
ruby bin/jekyll-page "Crawling Images using Sparkler" tut
```

Then edit the markdown file under `_posts/` directory.

Then follow the standard github contribution guideline.
If not already, fork this project from [https://github.com/USCDataScience/sparkler](https://github.com/USCDataScience/sparkler) to https://github.com/<yourId>/sparkler

```bash
git remote add own git@github.com/<yourId>/sparkler
git add docs/_posts/*
git commit -m 'Added documentation for ___'
git push own <branchname>
```

Then raise a pull request at [https://github.com/USCDataScience/sparkler](https://github.com/USCDataScience/sparkler) using the github web UI.

Contact developers on [slack](/sparkler/#slack) if you have questions.
