#!/usr/bin/env bash
# Publie le SNAPSHOT mobile com.leonardobishop:quests:<version>-SNAPSHOT (le jar du plugin, tâche allJar, pom sans
# dépendances) sur Reposilite (https://maven.skullbox.fr/snapshots). Lancé après le build :
#  - branche par défaut (master) : sans argument, <version committée>-SNAPSHOT (3.15.2-SNAPSHOT : code V1) ;
#  - branche v2 : avec l'argument « v2-SNAPSHOT » (ci.yml), un SNAPSHOT mobile distinct et stable
#    com.leonardobishop:quests:v2-SNAPSHOT, que lisent SkullboxUtils et SkullboxTreasure (branches v2).
# Usage : publish-snapshot.sh [version-SNAPSHOT]
#
# Version (sans argument) : build.gradle.kts porte la version de l'amont (sans -SNAPSHOT : on n'y touche pas, une
# synchronisation avec l'amont y apporte ses montées de version sans conflit). Le SNAPSHOT est donc
# <version committée sans -SNAPSHOT>-SNAPSHOT, passé par -PreleaseVersion. -Pgitversion=false : sans lui, allJar
# suffixerait la version du hash de commit (3.15.2-SNAPSHOT-abc1234) et chaque push créerait une NOUVELLE version.
# groupId/artifactId com.leonardobishop:quests inchangés : SkullboxUtils le lit.
# Seule la publication « maven » du projet racine existe (un seul artefact : le jar du plugin Bukkit).
#
# Pas d'élagage : Reposilite ne garde que le dernier build horodaté d'une version -SNAPSHOT.
# Environnement : MAVEN_USERNAME/MAVEN_TOKEN (jeton d'écriture Reposilite, dépôt « Reposilite » de
#                 build.gradle.kts).
set -euo pipefail

if [ $# -ge 1 ]; then
  version="$1"
  case "$version" in *-SNAPSHOT) ;; *) echo "::error::la version explicite doit finir par -SNAPSHOT ('$version')"; exit 1 ;; esac
  echo "SNAPSHOT explicite $version"
else
  committed=$(./gradlew -q --console=plain :properties -Pgitversion=false | sed -n 's/^version: //p')
  case "$committed" in ''|unspecified) echo "::error::version du build illisible ('$committed')"; exit 1 ;; esac
  version="${committed%-SNAPSHOT}-SNAPSHOT"
  echo "Version committée $committed -> SNAPSHOT $version"
fi

# « clean » : le jar publié est reconstruit avec la version SNAPSHOT (son plugin.yml la porte).
./gradlew --console=plain clean publishMavenPublicationToReposiliteRepository \
  -PreleaseVersion="$version" -Pgitversion=false -x test
