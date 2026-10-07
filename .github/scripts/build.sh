#!/usr/bin/env bash
# Build Gradle (tous les sous-projets, tests compris) en injectant la version de release : le jar du plugin
# (build/libs/Quests-<version>.jar, tâche allJar) et son plugin.yml portent <version>. Rien n'est committé.
#  - -PreleaseVersion : lu par build.gradle.kts (un -Pversion= serait écrasé par « version = » du script).
#  - -Pgitversion=false : neutralise le suffixe « -<hash de commit> » que allJar ajoute à la version
#    (3.15.2-4749c74) ; la Release porte donc une version propre, et le jar a un nom prévisible.
# « clean » : un seul Quests-<version>.jar dans build/libs (les globs de release et d'assets en dépendent).
# Usage : build.sh <version>
set -euo pipefail
version="${1:?usage: build.sh <version>}"

./gradlew --console=plain clean build -PreleaseVersion="$version" -Pgitversion=false
