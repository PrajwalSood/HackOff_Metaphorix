package com.shivam.emotions.util

import com.shivam.emotions.tfmodels.arousal.ClassificationArousal
import com.shivam.emotions.tfmodels.emotion.ClassificationEmotion
import com.shivam.emotions.tfmodels.valence.ClassificationValence

class EmotionUtil {
    companion object {
        fun compoundEmotion(
            emotion: ClassificationEmotion,
            arousal: ClassificationArousal,
            valence: ClassificationValence
        ): String {
            val emo: String
            when (emotion.title) {
                EmotionConstants.ANGRY -> {
                    emo = when {
                        arousal.title == 0 -> {
                            EmotionConstants.ANNOYED
                        }
                        valence.title == 0 -> {
                            EmotionConstants.DEFIANT
                        }
                        else -> {
                            EmotionConstants.ENVIOUS
                        }
                    }
                }

                EmotionConstants.CONTEMPT -> {
                    emo = if (arousal.title == 0) {
                        EmotionConstants.FRUSTRATED
                    } else {
                        EmotionConstants.HATEFUL
                    }
                }

                EmotionConstants.DISGUSTED -> {
                    emo = when {
                        arousal.title == 0 -> {
                            EmotionConstants.DISCONNECTED
                        }
                        arousal.title == 1 -> {
                            EmotionConstants.DISTRESSED
                        }
                        valence.title == 0 -> {
                            EmotionConstants.INDIGENT
                        }
                        else -> {
                            EmotionConstants.NAN
                        }
                    }
                }

                EmotionConstants.FEARFUL -> {
                    emo = when (arousal.title) {
                        0 -> {
                            EmotionConstants.JEALOUS
                        }
                        1 -> {
                            EmotionConstants.ALARMED
                        }
                        else -> {
                            EmotionConstants.NAN
                        }
                    }
                }

                EmotionConstants.HAPPY -> {
                    emo = when {
                        arousal.title == 0 -> {
                            EmotionConstants.CONTENT
                        }
                        arousal.title == 1 -> {
                            EmotionConstants.DELIGHTED
                        }
                        valence.title == 0 -> {
                            EmotionConstants.AMUSED
                        }
                        else -> {
                            EmotionConstants.NAN
                        }
                    }
                }

                EmotionConstants.NEUTRAL -> {
                    emo = when {

                        arousal.title == 1 && valence.title == 1 -> {
                            EmotionConstants.LHC
                        }
                        arousal.title == 1 && valence.title == 0 -> {
                            EmotionConstants.SUSPICIOUS
                        }
                        arousal.title == 0 && valence.title == 1 -> {
                            EmotionConstants.GUILTY
                        }
                        arousal.title == 0 && valence.title == 0 -> {
                            EmotionConstants.IC
                        }
                        else -> {
                            EmotionConstants.NAN
                        }
                    }
                }

                EmotionConstants.SAD -> {
                    emo = when {
                        arousal.title == 1 -> {
                            EmotionConstants.DISAPPOINTMENT
                        }
                        arousal.title == 0 -> {
                            EmotionConstants.ANXIOUS
                        }
                        valence.title == 1 -> {
                            EmotionConstants.DESPONDENT
                        }
                        valence.title == 0 -> {
                            EmotionConstants.DEPRESSED
                        }
                        else -> {
                            EmotionConstants.NAN
                        }
                    }
                }

                EmotionConstants.SURPRISED -> {
                    emo = when {
                        arousal.title == 0 -> {
                            EmotionConstants.AC
                        }
                        arousal.title == 1 -> {
                            EmotionConstants.AROUSED
                        }
                        valence.title == 1 -> {
                            EmotionConstants.EXCITED
                        }
                        else -> {
                            EmotionConstants.NAN
                        }
                    }
                }

                else -> {
                    emo = EmotionConstants.NAN
                }
            }
            return emo
        }
    }
}