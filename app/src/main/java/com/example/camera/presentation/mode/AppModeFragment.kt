package com.example.camera.presentation.mode

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.example.camera.R
import com.example.camera.databinding.FragmentModeChooserBinding
import com.example.camera.presentation.base.BaseFragment
import com.example.camera.presentation.mode.AppModeViewModel.ModeAction
import com.example.camera.utils.observe

class AppModeFragment : BaseFragment<FragmentModeChooserBinding, AppModeViewModel>() {

    override fun provideLayoutId() = R.layout.fragment_mode_chooser

    override fun provideViewModelClass() = AppModeViewModel::class.java

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        observe(viewModel.action) {
            when (it) {
                ModeAction.CAR_BRAND_CLASSIFICATION ->
                    findNavController().navigate(
                        AppModeFragmentDirections.actionAppModeFragmentToSetupFragment(isClassificationMode = true),
                        getTransitionExtras()
                    )
                ModeAction.DETECTION ->
                    findNavController().navigate(
                        AppModeFragmentDirections.actionAppModeFragmentToSetupFragment(),
                        getTransitionExtras()
                    )
            }
        }
    }

    private fun getTransitionExtras() = FragmentNavigatorExtras(
        binding.txtLogo to binding.txtLogo.transitionName,
        binding.cardOptions to binding.cardOptions.transitionName,
        binding.imgLogo to binding.imgLogo.transitionName
    )
}
