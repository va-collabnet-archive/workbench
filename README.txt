Workbench Sample Project Assembly

The default build will produce a standalone editor (without documentation).
This is intended to be the fastest build.

You can optionally build the following profiles:

  - sync: additionally builds the synchronized editor
  - viewer: additionally builds a viewer (NOT YET FUNCTIONAL)
  - documentation: builds the documentation and includes it in any produced editor or viewer
  - installer: builds installers for whichever editor(s) or viewer are produced
  - all: builds all 3 applications, with documentation. The installer profile must still be added separately to build
         installers.

The following optimizations can also be configured:

  - svn-db: enabled by default (except during a release), this will use an SVN copy of the berkeley-db to speed up
            builds. It can be disabled by running with -P-svn-db
  - svn-db-commit: disabled by default, this is intended for CI to be able to update the above database copy
  - generate-db: disabled by default (except during a release), this will generate a clean copy of the berkeley-db.
                 This is not currently connected to the svn-db profile, though it may be in future (being enabled
                 when SVN is not and vice-versa)
