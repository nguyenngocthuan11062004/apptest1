package com.example.apptest1.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.apptest1.databinding.FragmentOnboardingBinding
import com.example.apptest1.model.OnboardingPage

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    private lateinit var page: OnboardingPage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        page = requireArguments().getParcelable(ARG_PAGE)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.text = page.title
        binding.tvDescription.text = page.description
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PAGE = "ARG_PAGE"

        fun newInstance(page: OnboardingPage): OnboardingFragment {
            return OnboardingFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PAGE, page)
                }
            }
        }
    }
}
