<p align="center">
<img src="https://leonardobishop.com/~/artwork/questcompass2-256.png" width="200" height="200"><br>
<img src="https://img.shields.io/github/license/LMBishop/Quests">
<img src="https://img.shields.io/github/actions/workflow/status/LMBishop/Quests/build.yml?branch=master">
<img src="https://img.shields.io/github/issues-raw/LMBishop/Quests">
<img src="https://img.shields.io/spiget/version/23696?color=inactive&label=version">

[//]: # (<img src="https://mcbadges.leonardobishop.com/all/downloads?spigot=23696&songoda=quests-quests&polymart=938">)
<br>
<h1 align="center">Quests</h1>
</p>

#### Quick Navigation
- [Downloads / Building](#-downloads--building)
- [Contributors](#-contributors)
- [Support](#-support)
- [License](#-license)
- [Wiki](#-wiki)

## 💾 Downloads / Building
The latest release version of Quests can be found [here](https://quests.leonardobishop.com/download.html).
The latest build of Quests (development version) can be found on [GitHub](https://github.com/LMBishop/Quests/actions).

Alternatively, you can build Quests via Gradle. Release versions of Quests are built using **Gradle**, targeting **Java 17**. You can change the target version in ``build.gradle``.
* Ensure Java is installed on your machine
* Clone this repository
* Run ``./gradlew`` (Linux and macOS) or ``gradlew`` (Windows) in the base directory to build Quests
    * The jar will be output in `/build/libs`

*See [CONTRIBUTING.md](https://github.com/LMBishop/Quests/blob/master/CONTRIBUTING.md) for more information.*


#### 🧰 Custom Task
Creating new Task Types within Quests is supported, [see the wiki](https://quests.leonardobishop.com/developer/new-task-type.html) for help.

Quests can be found on the Maven repository listed below, or alternatively on [JitPack](https://jitpack.io/#LMBishop/Quests).

For versions from `repo.leonardobishop.com`, the **version number corresponds to the release version**. Please see Spigot for the latest release number.
#### 👨‍💻 Maven
```xml
<repository>
    <id>repo.leonardobishop.com</id>
    <url>https://repo.leonardobishop.com/releases/</url>
</repository>

<dependency>
    <groupId>com.leonardobishop</groupId>
    <artifactId>quests</artifactId>
    <version><!--LATEST SPIGOT VERSION--></version>
    <scope>provided</scope>
</dependency>
```

#### 👩‍💻 Gradle
```groovy
repositories {
    maven { url = uri('https://repo.leonardobishop.com/releases/') }
}

dependencies {
    compileOnly 'com.leonardobishop:quests:<LATEST SPIGOT VERSION>'
}
```

## 👫 Contributors
See https://github.com/LMBishop/Quests/graphs/contributors

#### 🤝 Contributing to Quests
See [CONTRIBUTING.md](https://github.com/LMBishop/Quests/blob/master/CONTRIBUTING.md)

## 📖 Wiki
Full documentation can be found at [https://quests.leonardobishop.com/](https://quests.leonardobishop.com/).

Documentation is built directly from this repository, from the `/docs` directory.

## 💡 Support
For support please open a [GitHub issue](https://github.com/LMBishop/Quests/issues) or join our [Discord server](https://discord.gg/mQ2RcJC). Please provide information of the issue, any errors that may come up and make sure you are using the latest version of the plugin.

#### ⁉️ Issue Tracker
**This is the preferred method of bug reporting & feature requests**. Please use one of the two templates which are provided. If it is neither a bug report or a feature request and is a question, Discord would be a better place to ask this instead.

#### 💬 Discord
**This is the preferred method for general questions about Quests or the development of the project**. There is no dedicated support team, rather a team of volunteers (myself) who can help only when they have time.

#### 🌐 Language
Please speak English and do not use any vulgar or harmful language. We work on this project in our free time, getting mad at us, making demands, or just complaining in general will not achieve anything.

## 📜 License
The **source code** for Quests is licensed under the GNU General Public License v3.0, to view the license click [here](https://github.com/LMBishop/Quests/blob/master/LICENSE.txt).

The **artwork** for Quests is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License ![](https://i.creativecommons.org/l/by-nc-sa/4.0/80x15.png), to learn more click [here](https://creativecommons.org/licenses/by-nc-sa/4.0/).


## Compiler en local (fork Skull-box)

Fork du réseau Skullbox, maintenu avec ses intégrations maison. Prérequis : JDK 21 (le wrapper Gradle est dans le
dépôt). **Un jeton GitHub est nécessaire** : quatre plugins du réseau (`skullboxutils`, `skullboxessentials`,
`skullboxtreasure`, `skullboxcrystals`) et `boxed-core` ne sont lisibles que depuis les GitHub Packages de l'org
Skull-box (SNAPSHOT mobiles, republiés par la CI de leurs dépôts). Jeton avec le droit `read:packages`
(`gh auth refresh -s read:packages`), dans la variable d'environnement `PACKAGES_READ_TOKEN` (ou `gpr.user` /
`gpr.key` dans `~/.gradle/gradle.properties`). Ne le commitez jamais.

```bash
export PACKAGES_READ_TOKEN=$(gh auth token)   # ou un jeton personnel read:packages
./gradlew clean build -Pgitversion=false
```

Le jar du plugin est `build/libs/Quests-3.15.2.jar` (`quests-3.15.2.jar`, minuscules, est le jar vide du projet
racine). `-PreleaseVersion=x.y.z` remplace la version de `build.gradle.kts`, et le jar comme `plugin.yml` la portent.
Sans `-Pgitversion=false`, le build garde le comportement de l'amont : la version est suffixée du hash de commit
(`Quests-3.15.2-<hash>.jar`).

Le type de tâche `nuvotifier_vote` a été retiré du fork : sa dépendance (`com.vexsoftware:NuVotifier`) n'existe plus
sur aucun dépôt Maven.

## CI/CD (Skull-box)

Fichiers : `.github/workflows/ci.yml`, `.github/scripts/`, `.releaserc.json`. Runner `blacksmith-2vcpu-ubuntu-2404`, JDK 21.

- **Chaque push et PR** : `./gradlew clean build` (tests compris). Une PR ne publie rien.
- **Push sur `master`** : release GitHub par [semantic-release](https://github.com/semantic-release/semantic-release)
  (`feat:` = mineure, `fix:` = correctif, `feat!:` ou `BREAKING CHANGE` = majeure ; `ci:`, `build:`, `docs:`, `chore:`,
  `refactor:` ne produisent aucune release). La Release porte `Quests-<version>.jar`, sans suffixe de hash. La première
  Release (`v3.15.2`) a été créée d'après la version de `build.gradle.kts`.
- **Push sur `master`, aussi** : le jar du plugin est publié en SNAPSHOT mobile `com.leonardobishop:quests:3.15.2-SNAPSHOT`
  sur `https://maven.pkg.github.com/Skull-box/Quests` (pom sans dépendances ; lu par SkullboxUtils). Élagage : au 10e
  dépôt du SNAPSHOT le paquet est supprimé puis republié (~1,4 Mo par publication, 14 Mo au plus).
- **Toute autre branche** : prérelease GitHub glissante `build-<branche>` (titre = nom de la branche), écrasée à
  chaque push, supprimée avec la branche.
