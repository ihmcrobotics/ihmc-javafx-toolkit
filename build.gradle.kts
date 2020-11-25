import org.apache.commons.lang3.SystemUtils

buildscript {
   dependencies {
      classpath("org.apache.commons:commons-lang3:3.9")
   }
}

plugins {
   id("us.ihmc.ihmc-build") version "0.22.0"
   id("us.ihmc.ihmc-ci") version "7.0"
   id("us.ihmc.ihmc-cd") version "1.14"
}

ihmc {
   group = "us.ihmc"
   version = "0.19.1"
   vcsUrl = "https://github.com/ihmcrobotics/ihmc-javafx-toolkit"
   openSource = true
   
   configureDependencyResolution()
   configurePublications()
}

mainDependencies {
   api("net.sf.trove4j:trove4j:3.0.3")
   api("org.apache.commons:commons-lang3:3.9")

   api("org.fxyz3d:fxyz3d:0.1.1") {
      exclude(group = "java3d", module = "vecmath")
      exclude(group = "org.slf4j", module = "slf4j-simple")
   }

   api("us.ihmc:euclid:0.15.1")
   api("us.ihmc:ihmc-yovariables:0.9.6")
   api("us.ihmc:ihmc-messager:0.1.5")
   api("us.ihmc:ihmc-graphics-description:0.19.1")
   api("us.ihmc:jassimp:4.0.0-ihmc5")

   var javaFXVersion = "11.0.2"
   api(ihmc.javaFXModule("base", javaFXVersion))
   api(ihmc.javaFXModule("controls", javaFXVersion))
   api(ihmc.javaFXModule("graphics", javaFXVersion))
   api(ihmc.javaFXModule("fxml", javaFXVersion))
   api(ihmc.javaFXModule("swing", javaFXVersion))
}

visualizersDependencies {
   api(ihmc.sourceSetProject("main"))
}
