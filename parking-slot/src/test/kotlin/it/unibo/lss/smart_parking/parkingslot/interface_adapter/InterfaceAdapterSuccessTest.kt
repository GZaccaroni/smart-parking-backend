package it.unibo.lss.smart_parking.parkingslot.interface_adapter

import com.mongodb.client.MongoClients
import io.github.gzaccaroni.smartparking.parking_slot.BuildConfig
import io.ktor.http.*
import it.unibo.lss.smart_parking.parkingslot.FillParkingSlotCollection
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours

class InterfaceAdapterSuccessTest {

    @Test
    fun interfaceAdapterSuccessTest() {
        val mongoAddress = BuildConfig.PARKING_SLOT_DB_CONNECTION_STRING
        val databaseName = "ParkingSystem"
        val collectionName = "parking-slot-test"

        val userId = ObjectId().toString()
        val slotId = FillParkingSlotCollection.eraseAndFillCollection(
            mongoAddress,
            databaseName,
            collectionName,
            1
        ).first()

        val mongoClient = MongoClients.create(mongoAddress)
        val collection = mongoClient.getDatabase(databaseName).getCollection(collectionName)

        val interfaceAdapter = InterfaceAdapter(collection)
        val occupyResult = interfaceAdapter.occupyParkingSlot(userId, slotId, Clock.System.now().plus(1.hours))

        val jsonElementExpected = mapOf(
            "successCode" to Json.parseToJsonElement("Success")
        )
        val jsonObjectExpected = JsonObject(jsonElementExpected)

        assertEquals(Pair(HttpStatusCode.OK, jsonObjectExpected), occupyResult)

        Thread.sleep(60000)

        val incrementResult = interfaceAdapter.incrementParkingSlotOccupation(userId, slotId, Clock.System.now().plus(3.hours))

        assertEquals(Pair(HttpStatusCode.OK, jsonObjectExpected), incrementResult)

        Thread.sleep(60000)
        val freeResult = interfaceAdapter.freeParkingSlot(slotId)

        assertEquals(Pair(HttpStatusCode.OK, jsonObjectExpected), freeResult)
    }
}