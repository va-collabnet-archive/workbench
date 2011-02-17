package org.dwfa.mojo.amt;

public enum TransformStages {
	MP_MODEL_TRANSFORMER(new MpModelTransformer());

	private TransformStages(ModelTransformer modelTransformer){
		this.modelTransformer = modelTransformer;
	}

	public ModelTransformer getModelTransformer() {
		return modelTransformer;
	}

	private ModelTransformer modelTransformer;
}
