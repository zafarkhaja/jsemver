Contribution Guidelines
=======================

Thank you for your interest in contributing to the Java SemVer library. All
contributions are welcome and appreciated.

There are mainly 3 ways in which you can contribute to the project:
1. make code changes to add features, fix bugs, correct typos, refactor, etc.
2. open new issues to suggest improvements, request features, report bugs, etc.
3. help around the repository with other people's issues and pull requests

New issues and pull requests can be submitted from the
[project's home page](https://github.com/zafarkhaja/jsemver) on GitHub.

Making Code Changes
-------------------

To make code contribution go smoothly and to ensure consistency and conceptual
integrity throughout the project there are some rules to keep in mind as you are
making code changes.

### Plan

Before making a code change, unless an obviously trivial one, it's highly
recommended to open an issue to discuss the problem you are trying to solve,
possible solutions and implementation details with maintainer(s).

If you intend to work on an existing issue, inform other participants of the
discussion about your intent to avoid situations where multiple similar pull
requests get submitted.

### Develop

To start coding fork the project and create a new topic branch. Under the current
branching workflow topic branches of types feature, bugfix, etc. are branched off
the latest release branch and hotfix branches are branched off the master branch.

Each topic branch should only contain code that is relevant to that topic.

Branches must be given meaningful names. It's a good idea to prefix it with
a branch type (feature, bugfix, hotfix, support, etc.) and add the issue number,
e.g. `feature/47-serializability`.

When writing the code, adhere to the project's code style. The project doesn't
follow any one particular code style, so use the existing code as a reference.

The submitted code must follow the Clean Code principles.

The submitted code must be covered with unit tests. Exercising TDD while writing
the code is highly encouraged.

If necessary, documentation must be also updated to reflect the code changes.

Once you are ready to commit, strive for small and self-contained commits to
logically group related code changes.

Commits must have informative and well-formed messages (see `git log` for some
examples).

### Submit

Before submitting, synchronize your work with the upstream branch using `git rebase`.

To submit your code create a pull request with a description of the change and
a reference to the issue it's related to, if any.

Once the pull request is reviewed to ensure adherence to the above rules and
accepted, the topic branch will be merged by the maintainer(s).

Unlike issues, pull requests should not be used to discuss the problem being
solved and/or alternative solutions.


Opening New Issues
------------------

In order to avoid duplicates make sure the subject isn't covered by any of the
existing issues, open or closed, before opening a new one.

When reporting a bug, provide as much context and relevant information as you can,
such as affected version, expected result, actual result and steps to reproduce,
ideally in the form of executable test case.

When requesting a feature, provide as much context and relevant information as
you can, such as a clear description of the feature and its use case, motivation
behind it, possible implementations, code examples, etc.

You can also use issues to request support or clarification.


Helping Around
--------------

Obviously writing code and opening new issues are not the only two ways in which
you can contribute. You can also contribute by helping out with other people's
issues and pull requests.

You can provide support on issues by labeling them, verifying provided information,
reproducing bugs, validating requests, answering support-related questions and
participating in discussions.

You can provide support on pull requests by reviewing and validating code changes
submitted by others.

And finally, you can help out by cleaning up stale issues and pull requests.
