package com.example.mosibit.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mosibit.adapter.SibiAdapter
import com.example.mosibit.data.Sibi
import com.example.mosibit.databinding.FragmentHomeBinding
import com.example.mosibit.ui.BugReportActivity
import com.example.mosibit.utils.SibiData


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding
    private var list: ArrayList<Sibi> = arrayListOf()
    private lateinit var sibiAdapter: SibiAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =
            FragmentHomeBinding.inflate(LayoutInflater.from(inflater.context), container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding?.fabBugReport?.setOnClickListener{
            val intent = Intent(activity, BugReportActivity::class.java)
            startActivity(intent)
        }

        list.addAll(SibiData.listData)
        sibiAdapter = SibiAdapter(list)
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        binding?.rvSibi?.apply {
            layoutManager = GridLayoutManager(context,2)
            adapter = sibiAdapter
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}