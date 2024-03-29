package com.example.bookhub


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.bookhub.adapter.DashBoardRecyclerAdapter
import com.example.bookhub.model.Book
import com.example.bookhub.util.ConnectionManager
import kotlinx.android.synthetic.main.recycler_favourite_single_row.*
import org.json.JSONException
import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class DashboardFragment : Fragment() {

    lateinit var recyclerDashboard: RecyclerView

    lateinit var layoutManager: RecyclerView.LayoutManager


    lateinit var recyclerAdapter: DashBoardRecyclerAdapter

    lateinit var progressLayout: RelativeLayout

    lateinit var progressBar: ProgressBar


    val bookInfoList = arrayListOf<Book>()

    val ratingComparator = Comparator<Book>{ book1 , book2->

       if ( book1.bookRating.compareTo(book2.bookRating,true)==0) {
           //sort according to the name if rating is same
           book1.bookName.compareTo(book2.bookName,true)
       } else{
           book1.bookRating.compareTo(book2.bookRating,true)
       }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment


        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        setHasOptionsMenu(true)

        recyclerDashboard = view.findViewById<RecyclerView>(R.id.recyclerDashboard)

        progressLayout = view.findViewById(R.id.progressLayout)

        progressBar = view.findViewById(R.id.ProgressBar)

        progressLayout.visibility = View.VISIBLE

        layoutManager = LinearLayoutManager(activity)

        val queue = Volley.newRequestQueue(activity as Context)

        val url = "http://13.235.250.119/v1/book/fetch_books/"

        if (ConnectionManager().checkConnectivity(activity as Context)) {
            val jsonObjectRequest = object : JsonObjectRequest(Request.Method.GET, url, null, Response.Listener {
                //Here We  will Handle the response
                try {
                    progressLayout.visibility = View.GONE
                    val success = it.getBoolean("success")
                    if (success) {
                        val data = it.getJSONArray("data")
                        for (i in 0 until data.length()) {
                            val bookJsonObject = data.getJSONObject(i)
                            val bookObject = Book(
                                bookJsonObject.getString("book_id"),
                                bookJsonObject.getString("name"),
                                bookJsonObject.getString("author"),
                                bookJsonObject.getString("rating"),
                                bookJsonObject.getString("price"),
                                bookJsonObject.getString("image")
                            )
                            bookInfoList.add(bookObject)
                            recyclerAdapter = DashBoardRecyclerAdapter(activity as Context, bookInfoList)

                            recyclerDashboard.adapter = recyclerAdapter
                            recyclerDashboard.layoutManager = layoutManager
                        }
                    } else {
                        Toast.makeText(activity as Context, "Some Error Occurred!!!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    Toast.makeText(activity as Context, "Some unexpected Error occurred !!!", Toast.LENGTH_SHORT).show()
                }


            }, Response.ErrorListener {

                //Here we will Handle The Errors
                if (activity != null)
                    Toast.makeText(activity as Context, "Volley  error occurred!!!", Toast.LENGTH_SHORT).show()
                println("Response is $it")

            }) {

                override fun getHeaders(): MutableMap<String, String> {

                    val headers = HashMap<String, String>()
                    headers["Content-Type"] = "application/json"
                    headers["token"] = "a943b2600b404c"

                    return headers
                }

            }
            queue.add(jsonObjectRequest)

        } else {
            //Internet is not available
            val dialog = AlertDialog.Builder(activity as Context)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection  is not Found")
            dialog.setPositiveButton("Open Settings") { text, listener ->
                val settingIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingIntent)
                activity?.finish()
            }
            dialog.setNegativeButton("Exit") { text, listener ->

                ActivityCompat.finishAffinity(activity as Activity)

            }
            dialog.create()
            dialog.show()
        }
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater?.inflate(R.menu.menu_dashboard,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item?.itemId
        if (id == R.id.action_sort) {

            Collections.sort(bookInfoList, ratingComparator)
            bookInfoList.reverse()

        }
        recyclerAdapter.notifyDataSetChanged()


        return super.onOptionsItemSelected(item)
    }

}
