package deserializer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import generators.Dto.Transaction
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.api.common.typeinfo.TypeInformation
import com.fasterxml.jackson.module.scala.DefaultScalaModule

class JSONValueDeserializationSchema extends DeserializationSchema[Transaction] with Serializable {

  override def deserialize(message: Array[Byte]): Transaction = {
    val json = new String(message)
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    mapper.readValue(json, classOf[Transaction])
  }

  override def isEndOfStream(nextElement: Transaction): Boolean = false

  override def getProducedType: TypeInformation[Transaction] = TypeInformation.of(classOf[Transaction])
}
