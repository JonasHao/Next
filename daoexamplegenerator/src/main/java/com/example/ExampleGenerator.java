package com.example;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;

public class ExampleGenerator {
    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(2, "com.jonashao.musicrecommender.models");
        addNode(schema);
        schema.enableKeepSectionsByDefault();
        new DaoGenerator().generateAll(schema, "F:\\Projects\\Android\\NextMusic\\musicrecommender\\src\\main\\java-gen");
    }

    private static void addNode(Schema schema) {

        Entity subjection = schema.addEntity("Subjection");
        subjection.addIdProperty().getProperty();
        Property DOM = subjection.addFloatProperty("DegreeOfMembership").getProperty();
        Property musicID = subjection.addLongProperty("musicID").notNull().getProperty();
        Property clusterID = subjection.addLongProperty("clusterID").notNull().getProperty();

        Entity recorder = schema.addEntity("Recorder");
        recorder.addIdProperty().getProperty();
        recorder.addIntProperty("ActionCode");
        Property actionDate = recorder.addDateProperty("ActionDate").getProperty();
        Property musicIDRecorder = recorder.addLongProperty("MusicID").notNull().getProperty();

        Entity music = schema.addEntity("Music");
        music.addIdProperty().getProperty();
        music.addBooleanProperty("Love");
        music.addLongProperty("RID").unique().notNull();
        music.addStringProperty("Title").notNull();
        music.addStringProperty("Artist").notNull();
        music.addLongProperty("ArtistID");
        music.addStringProperty("CoverPath");
        music.addIntProperty("Duration");
        music.addStringProperty("Album");
        music.addLongProperty("AlbumID");
        music.addIntProperty("Year");
        music.addStringProperty("Genre");
        music.addToMany(subjection, musicID).orderDesc(DOM);
        music.addToMany(recorder, musicIDRecorder).orderDesc(actionDate);

        Entity cluster = schema.addEntity("Cluster");
        cluster.addIdProperty().getProperty();
        cluster.addDoubleProperty("Latitude");
        cluster.addDoubleProperty("Longitude");
        cluster.addDoubleProperty("Altitude");
        cluster.addShortProperty("Hour");
        cluster.addToMany(subjection, clusterID).orderDesc(DOM);


    }


}

