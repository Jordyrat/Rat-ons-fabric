# Working in this repository

## Pull request titles

Release notes are drafted from pull request titles, and the labeler reads keywords from the title, so every title starts with one of these:

- `fix:` — a bug fix, labelled `bug`
- `feature:` or `feat:` — new behaviour, labelled `feature`
- `improvement:` — a change to something that already works, labelled `improvement`
- `docs:` — documentation only, labelled `documentation`
- `backend:` — build, workflow or tooling, labelled `backend`

The labeler strips the prefix afterwards, so the release notes read as plain sentences. A pull request with no matching keyword gets no label and is left out of the release draft entirely: `.github/release-drafter.yml` only includes `feature`, `improvement` and `bug`.

Retitle an older pull request that missed this, since it is the title at merge time that the drafter picks up.
