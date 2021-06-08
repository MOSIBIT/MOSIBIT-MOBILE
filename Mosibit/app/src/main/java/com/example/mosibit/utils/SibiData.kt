package com.example.mosibit.utils

import com.example.mosibit.R
import com.example.mosibit.data.Sibi

object SibiData {
    private val title = arrayOf(
        "A",
        "B",
        "C",
        "D",
        "E",
        "F",
        "G",
        "H",
        "I",
        "J",
        "K",
        "L",
        "M",
        "N",
        "O",
        "P",
        "Q",
        "R",
        "S",
        "T",
        "U",
        "V",
        "W",
        "X",
        "Y",
        "Z"
        )

    private val image = intArrayOf(
        R.drawable.a,
        R.drawable.b,
        R.drawable.c,
        R.drawable.d,
        R.drawable.e,
        R.drawable.f,
        R.drawable.g,
        R.drawable.h,
        R.drawable.i,
        R.drawable.j,
        R.drawable.k,
        R.drawable.l,
        R.drawable.m,
        R.drawable.n,
        R.drawable.o,
        R.drawable.p,
        R.drawable.q,
        R.drawable.r,
        R.drawable.s,
        R.drawable.t,
        R.drawable.u,
        R.drawable.v,
        R.drawable.w,
        R.drawable.x,
        R.drawable.y,
        R.drawable.z
    )

    val listData : ArrayList<Sibi>
            get(){
                val list = arrayListOf<Sibi>()
                for (posistion in title.indices){
                    val sibi = Sibi()
                    sibi.Image = image[posistion]
                    sibi.title = title[posistion]
                    list.add(sibi)
                }
                return list
            }
}