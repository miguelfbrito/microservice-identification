from enum import Enum


class WeightType(Enum):

    STRUCTURAL = 'weight_structural'
    TF_IDF = 'weight_tf_idf'
    LDA = 'weight_lda'
    ABSOLUTE = 'weight'
    METHOD_CALL = 'weight_method_call'

    def __str__(self):
        return str(self.value)

    def __repr__(self):
        return str(self.value)
