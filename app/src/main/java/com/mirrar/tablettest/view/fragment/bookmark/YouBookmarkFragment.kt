package com.mirrar.tablettest.view.fragment.bookmark

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentTransaction
import com.mirrar.tablettest.R
import com.mirrar.tablettest.databinding.FragmentYouBookmarkBinding
import com.mirrar.tablettest.utility.Bookmarks
import com.mirrar.tablettest.view.fragment.bookmark.adapter.BookmarkAdapter
import com.mirrar.tablettest.view.fragment.email.EmailPopupFragment

class YouBookmarkFragment : DialogFragment() {

    private var _binding: FragmentYouBookmarkBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentYouBookmarkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.closeView.setOnClickListener {
            dismissDialog()
        }

        binding.imageView2.setOnClickListener {
            dismissDialog()
        }

        val bookmarkAdapter = BookmarkAdapter()
        Bookmarks.bookmarks.observe(viewLifecycleOwner) { bookmarkedProducts ->
            if (bookmarkedProducts == null) {
                return@observe
            }
            bookmarkAdapter.updateData(bookmarkedProducts)
        }

        binding.productRecycler.adapter = bookmarkAdapter

        binding.email.setOnClickListener {
            openDialogFragment(EmailPopupFragment.newInstance("wishlist"))
        }
    }

    private fun openDialogFragment(fragment: DialogFragment) {
        fragment.show(childFragmentManager, fragment.tag)
    }

    private fun dismissDialog() {
        parentFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
            .remove(this)
            .commit()
    }

    companion object {
        @JvmStatic
        fun newInstance() = YouBookmarkFragment()
    }
}