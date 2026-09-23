# Bundled browser dependencies

These runtime assets are served locally so login, CSRF submission, calendars and uploads do not depend on external CDNs.

- jQuery 3.7.1 (MIT)
- Bootstrap 5.3.3 (MIT; bundle includes Popper)
- Bootstrap Icons 1.10.5 (MIT)
- FullCalendar 5.11.5 standard bundle (MIT)

Original license files are included beside each distribution.
Reproduce with a temporary npm installation of the exact versions above, then run
`node scripts/vendor-assets.cjs <temporary node_modules directory>`.
Do not edit minified vendor code by hand.
