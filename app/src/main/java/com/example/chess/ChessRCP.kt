package com.example.chess

import android.net.Uri
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.example.chess.ChessGrpcKt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.delay
import java.io.Closeable
import io.grpc.example.chess.Table

class ChessRCP(uri: Uri) : Closeable {

    private val channel : ManagedChannel
    init {
        val builder = ManagedChannelBuilder.forAddress(uri.host, uri.port)
        if (uri.scheme == "https") {
            builder.useTransportSecurity()
        } else {
            builder.usePlaintext()
        }
        channel =  builder.executor(Dispatchers.IO.asExecutor()).build()
    }
    /*
    private val channel = let {
        val builder = ManagedChannelBuilder.forAddress(uri.host, uri.port)
        if (uri.scheme == "https") {
            builder.useTransportSecurity()
        } else {
            builder.usePlaintext()
        }
        builder.executor(Dispatchers.IO.asExecutor()).build()
    }
     */

    private val chessRequest = ChessGrpcKt.ChessCoroutineStub(channel)

    suspend fun getNextStep(currentMove: String) :String {
        try {
            val request = io.grpc.example.chess.move {
                move = currentMove
            }
            val response = chessRequest.getNextMove(request)
            return response.move
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    suspend fun getTables() :List<Table>? {
        try {
            val request = io.grpc.example.chess.noparam {
            }
            val response = chessRequest.getTables(request)
            return response.tablesList
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    suspend fun setTable(table: Table) :List<Table>? {
        try {
            val request = table
            val response = chessRequest.setTable(request)
            return response.tablesList
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun close() {
        channel.shutdownNow()
    }

    companion object {
        private lateinit var INSTANCE: ChessRCP
        @JvmStatic
        fun getInstance(uri: Uri): ChessRCP {
            if (!::INSTANCE.isInitialized) {
                INSTANCE = ChessRCP(uri)
            }
            return INSTANCE
        }
        fun getInstance(): ChessRCP {
            return INSTANCE
        }
    }

}
