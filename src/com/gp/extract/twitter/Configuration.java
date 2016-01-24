package com.gp.extract.twitter;

import java.util.EnumMap;

/**
 * Created by tarab on 1/8/2016.
 */
public class Configuration {
    private static EnumMap<Task, String> TRAIN_FILES = new EnumMap<Task, String>(Task.class);
    private static EnumMap<Task, String> TEST_FILES = new EnumMap<Task, String>(Task.class);
    private static EnumMap<Task, String> TAGGER_FOLDER = new EnumMap<Task, String>(Task.class);
    private static EnumMap<Task, Double> L1_REGULARIZATION = new EnumMap<Task, Double>(Task.class);
    private static EnumMap<Task, Double> L2_REGULARIZATION = new EnumMap<Task, Double>(Task.class);


    public static enum Task {
        POS,
        CHUNKER
    }

    static{
        /*********  Training files  *********/
        TRAIN_FILES.put(Task.POS, "data/oct27.conll");

        /*********  Testing files  *********/
        TEST_FILES.put(Task.POS, "data/daily547.conll");

        /*********  Tagger Folder  *********/
        TAGGER_FOLDER.put(Task.POS, "pos_model_memm/");

        /*********  L1 Regularization  *********/
        L1_REGULARIZATION.put(Task.POS, 0.25);

        /*********  L2 Regularization  *********/
        L2_REGULARIZATION.put(Task.POS, 2.0);
    }
    private Configuration(){

    }

    public static String getTrainingFile(Task task)
    {
        return TRAIN_FILES.get(task);
    }

    public static String getTestingFile(Task task)
    {
        return TEST_FILES.get(task);
    }

    public static double getL1(Task task)
    {
        return L1_REGULARIZATION.get(task);
    }

    public static double getL2(Task task)
    {
        return L2_REGULARIZATION.get(task);
    }

    public static String getTaggerFolder(Task task)
    {
        return TAGGER_FOLDER.get(task);
    }

}
