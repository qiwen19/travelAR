package com.ict3104.t10_idk_2020;

import android.content.Context;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;

import static android.graphics.Color.RED;

public class ObjNode extends Node {
    Context context;
    PlaceToken place;

    private ModelRenderable redSpehereRenderable = null;
    private ModelRenderable checkedRenderable = null;

    public ObjNode(Context c, PlaceToken p){
        this.context = c;
        this.place = p;
    }

    @Override
    public void onActivate() {
        super.onActivate();

        if(getScene() == null){
            return;
        }

        if(checkedRenderable != null){
            return;
        }

        MaterialFactory.makeOpaqueWithColor(context, new Color(RED))
                .thenAccept((material) -> {
                    redSpehereRenderable =
                            ShapeFactory.makeSphere(
                                    0.4f, Vector3.zero(), material
                            );
                    checkedRenderable = redSpehereRenderable;
                    setRenderable(checkedRenderable);
                });

    }
}
