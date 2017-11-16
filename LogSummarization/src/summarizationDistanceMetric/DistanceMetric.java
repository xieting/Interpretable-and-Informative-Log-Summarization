package summarizationDistanceMetric;

import feature_management.FeatureVector;

public interface DistanceMetric{
      public double dist(FeatureVector left,FeatureVector right);
}
