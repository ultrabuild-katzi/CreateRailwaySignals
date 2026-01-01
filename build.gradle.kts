plugins {
    id ("fabric-loom") version "1.10-SNAPSHOT"
    id ("me.modmuss50.mod-publish-plugin") version "0.7.4"
    id ("maven-publish")
}


version = project.property("mod_version")!!
group = project.property("maven_group")!!

base {
    archivesName.set(property("archives_base_name").toString())
}

loom {
    accessWidenerPath = file("src/main/resources/create_railway_signal.accesswidener")
}


repositories {
    maven { url = uri("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")}
    maven { // Ponder, Flywheel//
        name= "createmod maven"
        url= uri("https://maven.createmod.net/")
    }
    maven { name = "Curseforge Maven"; description = "Forge Config API Port"; url = uri("https://cursemaven.com/")}
    maven {  // Flywheel, Registrate, Create
        url = uri("https://maven.tterrag.com/")
        content {
            includeGroup("com.simibubi.create")
            includeGroup("com.tterrag.registrate")
            includeGroup("com.jozufozu.flywheel")
        }
    }

    maven {
        name = "TerraformersMC"
        url = uri("https://maven.terraformersmc.com/")
    }
    maven {
        name = "Ladysnake Libs"
        url = uri("https://maven.ladysnake.org/releases")
    }

    maven {
        name = "Fuzs Mod Resources"
        url = uri("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/")
    }
    maven { url = uri("https://mvn.devos.one/snapshots/")} // Create Fabric, Porting Lib, Forge Tags, Milk Lib, Registrate Fabric
    maven { url = uri("https://mvn.devos.one/releases/")} // Porting Lib

    maven { name = "JamiesWhiteShirt Maven"; description = "Reach Entity Attributes"; url = uri("https://maven.jamieswhiteshirt.com/libs-release")}


    maven { name = "Jitpack maven"; description = "Mixin Extras & Fabric ASM"; url = uri("https://jitpack.io/") } //NOTE: LEAVE THIS AS LAST
}

publishMods {
    file.set(tasks.remapJar.get().archiveFile)
    changelog.set (project.property("changelog").toString())
    type.set (me.modmuss50.mpp.ReleaseType.STABLE)
    modLoaders.add("fabric")
    modLoaders.add("quilt")

    modrinth {
        projectId = "3SNH7L6V"
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        minecraftVersions.add(project.property("minecraft_version").toString())

        requires("fabric-api")
        requires("create-fabric")
        requires("flywheel")
        optional("grenzzeichen")
    } 

}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft ("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings ("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
    modImplementation ("net.fabricmc:fabric-loader:${project.property("loader_version")}")

//    modImplementation("com.simibubi.create:create-fabric-${project.property("minecraft_version")}:${project.property("create_fabric_version")}+mc${project.property("minecraft_version")}")
    modImplementation("com.simibubi.create:create-fabric:6.0.8.0+build.1734-mc1.20.1")

//    modCompileOnly("dev.engine-room.flywheel:flywheel-fabric-$minecraft_version:$flywheel_version")
//    modCompileOnly("com.jozufozu.flywheel:flywheel-fabric-${project.property("minecraft_version")}:${project.property("flywheel_version")}")

    modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.1")

    modImplementation ("dev.emi:trinkets:${project.property("trinkets_version")}")

//    modImplementation(files("lib/grenzzeichen-1.0.1.jar"))
    modImplementation ("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", project.property("minecraft_version"))
    inputs.property("loader_version", project.property("loader_version"))
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(mapOf(
            "version" to project.version,
            "minecraft_version" to project.property("minecraft_version"),
            "loader_version" to project.property("loader_version"),
            "create_fabric_version" to "${project.property("create_fabric_version")}+${project.property("minecraft_version")}"
        ))
    }
}

val targetJavaVersion = 17
tasks.withType<JavaCompile> {
    // ensure that the encoding is set to UTF-8, no matter what the system default is
    // this fixes some edge cases with special characters not displaying correctly
    // see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
    // If Javadoc is generated, this must be specified in that task too.
    options.encoding = "UTF-8"
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible()) {
        options.release.set(targetJavaVersion)
    }
}

tasks.named<JavaExec>("runClient") {
    systemProperty ("mixin.env.remapRefMap", "true")
    systemProperty ("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")
}

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion= JavaLanguageVersion.of(targetJavaVersion)
    }
    withSourcesJar()
}


tasks.jar {
    from("LICENSE") {
        rename { "${it}_${base.archivesName.get()}" }
    }
}

// configure the maven publication
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = property("archives_base_name").toString()
            from(components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
        // Notice: This block does NOT have the same function as the block in the top level.
        // The repositories here will be used for publishing your artifact, not for
        // retrieving dependencies.
    }
}