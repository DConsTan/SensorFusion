package ch.ubiment.sensors.sensordemo;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by emmanuel on 08.06.17.
 */

public class Scene {
    private Cube mCube_G = new Cube(1.0f, 0.0f, 0.0f, 0.3f);
    private Cube mCube_M = new Cube(0.0f, 1.0f, 0.0f, 0.3f);

    private ArrayList<Line> vectorList = new ArrayList<>();
    private int NbVector = 0;

    private float[] axisVertices = {
            0.0f, 0.0f, 0.0f,
            2.0f, 0.0f, 0.0f,
            0.0f, 2.0f, 0.0f,
            0.0f, 0.0f, 2.0f,
    };
    private short[] axisIndices = {
            0, 1,
            0, 2,
            0, 3
    };
    private float[] axisRGBA = {
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.3f, 0.3f, 1.0f, 1.0f
    };
    private Line mAxis = new Line(axisVertices, axisIndices, axisRGBA);

    public Cube getmCube_G() {
        return mCube_G;
    }

    public Cube getmCube_M() {
        return mCube_M;
    }

    public Line getmAxis() {
        return mAxis;
    }

    public ArrayList<Line> getVectorList(){
        return vectorList;
    }


    public int newVector(float[] xyz, float[] rgba){
        int vectorID = NbVector;
        NbVector++;

        float[] tmpVertice = {
                xyz[0], xyz[1], xyz[2],
                0.0f, 0.0f, 0.0f
        };
        float[] tmpColors = {
                rgba[0],rgba[1],rgba[2],rgba[3],
                rgba[0],rgba[1],rgba[2],rgba[3]
        };
        short[] tmpIndices = {0,1};
        vectorList.add(vectorID, new Line(tmpVertice, tmpIndices, tmpColors));
        return vectorID;
    }

    public void setVectorXYZ(int ID, float[] xyz){
        float[] tmpVertice = {
                xyz[0], xyz[1], xyz[2],
                0.0f, 0.0f, 0.0f
        };
        vectorList.get(ID).setVertices(tmpVertice);
    }

    public Scene(){

    }

}
