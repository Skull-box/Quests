#!/usr/bin/env bash
# Publie le SNAPSHOT mobile com.leonardobishop:quests:<version>-SNAPSHOT (3.15.2-SNAPSHOT : le jar du plugin, tâche
# allJar, pom sans dépendances) sur GitHub Packages, après avoir élagué. Lancé après le build, sur la branche par défaut.
#
# Version : build.gradle.kts porte la version de l'amont (3.15.2, sans -SNAPSHOT : on n'y touche pas, une
# synchronisation avec l'amont y apporte ses montées de version sans conflit). Le SNAPSHOT est donc
# <version committée sans -SNAPSHOT>-SNAPSHOT, passé par -PreleaseVersion. -Pgitversion=false : sans lui, allJar
# suffixerait la version du hash de commit (3.15.2-SNAPSHOT-abc1234) et chaque push créerait une NOUVELLE version.
# groupId/artifactId com.leonardobishop:quests inchangés : le paquet appartient à ce dépôt et SkullboxUtils le lit.
# Seule la publication « maven » du projet racine existe (un seul artefact : le jar du plugin Bukkit).
#
# Élagage : chaque publication AJOUTE des fichiers horodatés (quests-3.15.2-AAAAMMJJ.hhmmss-N.*) à la MÊME version
# -SNAPSHOT du paquet ; l'API ne permet pas d'en supprimer un seul. Quand le compteur de publications
# (buildNumber de maven-metadata.xml) atteint SNAPSHOT_MAX, on supprime donc le paquet entier, puis on republie : il
# repart à 1 publication.
# ATTENTION : l'API supprime le paquet ENTIER, donc aussi la version fixe « amorcée » 3.15.2-4749c74 (jar de
# production) tant que SkullboxUtils la lit ; SkullboxUtils doit pointer sur 3.15.2-SNAPSHOT (voir README).
# Quota : 500 Mo pour toute l'org. SNAPSHOT_MAX = floor(20 Mo / taille d'une publication), plafonné à 10 (ci.yml).
# Environnement : GH_TOKEN (packages: write), MAVEN_USERNAME/MAVEN_TOKEN (auth du dépôt « GitHubPackages » de
#                 build.gradle.kts), GITHUB_REPOSITORY ; SNAPSHOT_MAX (défaut 10).
set -euo pipefail
max="${SNAPSHOT_MAX:-10}"
org="${GITHUB_REPOSITORY%%/*}"
registry="https://maven.pkg.github.com/$GITHUB_REPOSITORY"
# groupId:artifactId de la publication « maven » de build.gradle.kts
packages=(com.leonardobishop:quests)

committed=$(./gradlew -q --console=plain :properties -Pgitversion=false | sed -n 's/^version: //p')
case "$committed" in ''|unspecified) echo "::error::version du build illisible ('$committed')"; exit 1 ;; esac
version="${committed%-SNAPSHOT}-SNAPSHOT"
echo "Version committée $committed -> SNAPSHOT $version"

for p in "${packages[@]}"; do
  group="${p%%:*}"; artifact="${p##*:}"
  meta="$registry/${group//.//}/$artifact/$version/maven-metadata.xml"
  n=$(curl -sS -u "x:$GH_TOKEN" "$meta" | sed -n 's#.*<buildNumber>\([0-9]*\)</buildNumber>.*#\1#p' | head -1)
  n="${n:-0}"
  echo "$group.$artifact $version : $n publication(s) depuis la création de la version"
  if [ "$n" -ge "$max" ]; then
    echo "Élagage : suppression du paquet $group.$artifact (seuil $max)"
    gh api -X DELETE "orgs/$org/packages/maven/$group.$artifact"
  fi
done

# « clean » : le jar publié est reconstruit avec la version SNAPSHOT (son plugin.yml la porte).
./gradlew --console=plain clean publishMavenPublicationToGitHubPackagesRepository \
  -PreleaseVersion="$version" -Pgitversion=false -x test
