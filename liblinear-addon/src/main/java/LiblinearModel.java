/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import opennlp.tools.ml.model.MaxentModel;
import opennlp.tools.util.model.ArtifactSerializer;
import opennlp.tools.util.model.SerializableArtifact;
import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;

// TODO: The features need to be serialized with the model
// the liblinear model only contains the ints and weights,
// but the string lables get lost ... basically that are two maps.

// One for outcomes, one for the features ...

public class LiblinearModel implements MaxentModel, SerializableArtifact {

  private Model model;
  
  // Lets read them from disk, when model is loaded ... 
  private String outcomeLabels[];
  private Map<String, Integer> predMap;
  
  public LiblinearModel(Model model, String outcomes[], Map<String, Integer> predMap) {
    this.model = model;
    this.outcomeLabels = outcomes;
    this.predMap = predMap;
  }

  public LiblinearModel(InputStream in) throws IOException {
    model = Linear.loadModel(new InputStreamReader(in));
  }

  public double[] eval(String[] features) {
    
    // Note: If a feature can't be mapped, it will be ignored!
    
    List<Integer> context = new ArrayList<Integer>(features.length);
    
    for (int i = 0; i < features.length; i++) {
      Integer feature = predMap.get(features[i]);
      
      if (feature != null) {
        context.add(feature);
      }
    }
    
    return eval(context);
  }

  public double[] eval(String[] context, double[] probs) {
    return eval(context);
  }

  public double[] eval(String[] context, float[] values) {
    return eval(context);
  }

  private double[] eval(List<Integer> context) {
    
    double outcomes[] = new double[outcomeLabels.length];
    
    Feature vx[] = new Feature[context.size()];
    
    for (int i = 0; i < context.size(); i++) {
      vx[i] = new FeatureNode(context.get(i) + 1, 1d);
    }
    
    Linear.predictProbability(model, vx, outcomes);
    
    return outcomes;
  }
  
  public String getAllOutcomes(double[] outcomes) {
    // TODO: Return prev outcomes ..
    return null;
  }

  public String getBestOutcome(double[] ocs) {
    int best = 0;
    for (int i = 1; i < ocs.length; i++)
        if (ocs[i] > ocs[best]) best = i;
    return outcomeLabels[best];
  }

  // TODO: This method needs to go away from the interface ... !!!
  public Object[] getDataStructures() {
    return null;
  }

  public int getIndex(String outcome) {
    for (int i = 0; i < outcomeLabels.length; i++) {
      if (outcomeLabels[i].equals(outcome)) {
        return i;
      }
    }
    
    return -1;
  }

  public int getNumOutcomes() {
    return outcomeLabels.length;
  }

  public String getOutcome(int i) {
    return outcomeLabels[i];
  }

  public void serialize(OutputStream out) throws IOException {
    
  }
  
  public Class<?> getSerializerClass() {
    return LiblinearModelSerializer.class;
  }

}
