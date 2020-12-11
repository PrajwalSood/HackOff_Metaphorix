package com.shivam.emotions

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnticipateOvershootInterpolator
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.shivam.emotions.databinding.FragmentSplashBinding


class SplashFragment : BaseFragment() {
    private lateinit var splashBinding: FragmentSplashBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        splashBinding = FragmentSplashBinding.inflate(inflater, container, false)
        return splashBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val handler = Handler(Looper.getMainLooper())
        setObjectAnimation()



        handler.postDelayed({
            findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToHomeFragment())
        }, 3000)


    }

    private fun setObjectAnimation() {

        val icon = splashBinding.ivAppLogo

        val objectAnimator10 =
            ObjectAnimator.ofFloat(icon, "scaleX", 0.5f, 1f).setDuration(1000)
        val objectAnimator11 =
            ObjectAnimator.ofFloat(icon, "scaleY", 0.5f, 1f).setDuration(1000)


        objectAnimator10.interpolator = AnticipateOvershootInterpolator()
        objectAnimator11.interpolator = AnticipateOvershootInterpolator()

        val animatorSet1 = AnimatorSet()
        animatorSet1.playTogether(
            objectAnimator10,
            objectAnimator11
        )
        animatorSet1.start()
    }

}