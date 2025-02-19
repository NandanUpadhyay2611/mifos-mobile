package org.mifos.mobile.ui.fragments

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.github.therajanmaurya.sweeterror.SweetUIErrorHandler
import org.mifos.mobile.R
import org.mifos.mobile.models.FAQ
import org.mifos.mobile.presenters.HelpPresenter
import org.mifos.mobile.ui.activities.base.BaseActivity
import org.mifos.mobile.ui.adapters.FAQAdapter
import org.mifos.mobile.ui.fragments.base.BaseFragment
import org.mifos.mobile.ui.views.HelpView
import org.mifos.mobile.utils.Constants
import org.mifos.mobile.utils.DividerItemDecoration
import java.util.*
import javax.inject.Inject

/*
~This project is licensed under the open source MPL V2.
~See https://github.com/openMF/self-service-app/blob/master/LICENSE.md
*/
class HelpFragment : BaseFragment(), HelpView {
    @kotlin.jvm.JvmField
    @BindView(R.id.rv_faq)
    var rvFaq: RecyclerView? = null


    @kotlin.jvm.JvmField
    @BindView(R.id.layout_error)
    var layoutError: View? = null

    @kotlin.jvm.JvmField
    @Inject
    var faqAdapter: FAQAdapter? = null

    @kotlin.jvm.JvmField
    @Inject
    var presenter: HelpPresenter? = null
    private var rootView: View? = null
    private var faqArrayList: ArrayList<FAQ?>? = null
    private lateinit var sweetUIErrorHandler: SweetUIErrorHandler

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_help, container, false)
        setHasOptionsMenu(true)
        (activity as BaseActivity?)?.activityComponent?.inject(this)
        ButterKnife.bind(this, rootView!!)
        presenter?.attachView(this)
        setToolbarTitle(getString(R.string.help))
        sweetUIErrorHandler = SweetUIErrorHandler(activity, rootView)
        showUserInterface()
        if (savedInstanceState == null) {
            presenter?.loadFaq()
        }
        return rootView
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(Constants.HELP, ArrayList<Parcelable?>(faqArrayList))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            val faqs: ArrayList<FAQ?> = savedInstanceState.getParcelableArrayList(Constants.HELP) ?: arrayListOf()
            showFaq(faqs)
        }
    }

    private fun showUserInterface() {
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        rvFaq?.layoutManager = layoutManager
        rvFaq?.addItemDecoration(DividerItemDecoration(activity,
                layoutManager.orientation))
        rootView?.findViewById<Button>(R.id.call_button)?.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:" + getString(R.string.help_line_number))
            startActivity(intent)
        }
        rootView?.findViewById<Button>(R.id.mail_button)?.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.contact_email)) )
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.user_query))
            }
            try {
                startActivity(intent)
            }
            catch (e: Exception){
                Toast.makeText(requireContext(), getString(R.string.no_app_to_support_action),Toast.LENGTH_SHORT).show();
            }
        }
        rootView?.findViewById<Button>(R.id.locations_button)?.setOnClickListener {
            (activity as BaseActivity?)?.replaceFragment(LocationsFragment.newInstance(),true, R.id.container)
        }
    }

    override fun showFaq(faqArrayList: ArrayList<FAQ?>?) {
        faqAdapter?.setFaqArrayList(faqArrayList)
        rvFaq?.adapter = faqAdapter
        this.faqArrayList = faqArrayList
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_help, menu)
        val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.menu_search_faq).actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                val filteredFAQList = presenter?.filterList(faqArrayList, newText)
                filteredFAQList?.let {
                    if (it.isNotEmpty()) {
                        sweetUIErrorHandler.hideSweetErrorLayoutUI(rvFaq, layoutError)
                        faqAdapter?.updateList(it)
                    } else {
                        showEmptyFAQUI()
                    }
                } ?: showEmptyFAQUI()
                return false
            }
        })
    }

    private fun showEmptyFAQUI() {
        sweetUIErrorHandler.showSweetEmptyUI(getString(R.string.questions),
                R.drawable.ic_help_black_24dp, rvFaq, layoutError)
    }

    override fun showProgress() {
        showProgressBar()
    }

    override fun hideProgress() {
        hideProgressBar()
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun newInstance(): HelpFragment {
            val fragment = HelpFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}