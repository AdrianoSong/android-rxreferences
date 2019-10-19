package com.br.song.example.rxreferences

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    private val source = PublishSubject.create<String>()
    private val disposables = CompositeDisposable()
    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        callForPublishSubjectExample()
        configureCountButton()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    private fun configureCountButton() {
        buttonCount.setOnClickListener {
            source.onNext("Count " + ++count)
            if (count == 10) {
                textViewCount.text = "finished count $count"
                source.onComplete()
            }
        }
    }

    private fun callForPublishSubjectExample() {
        disposables.add(
            source.subscribe(
                { data -> textViewCount.text = data },
                { error -> textViewCount.text = "Error: $error" }
            )
        )
    }
}
