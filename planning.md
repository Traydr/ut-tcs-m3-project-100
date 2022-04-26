```mermaid
classDiagram
    direction LR
    
    class MyProtocol {
        +protocol.MyProtocol()
        +main(args)
    }
    
    class Packet {
        -int source
        -int destination
        -int packetType
        -int seqNr
        -int ackNr
        -int dataLen
        -byte[] data
        +decode()
        +makePkt()
    }
    
    class MediumAccessControl{
        -boolean sentPacket
        -MessageType previousMediumState
        
        +canWeSend()
    }
    
    class TextSplit{
        +textToBytes()
        +splitTextBytes()
        +arrayOfArrayBackToText()
    }
    
    class Forwarding{
        +init()
        +pathfinding()
        +addStep()
        +pathfinding()
        +matrixToArray()
        +arrayToMatrix()
    }
    
    class receiveThread{
        +receiveThread()
        +printByBuffer()
        +run()
    }
    
    class Node{
        -int address
    }
    
    class TimeOut{
    -boolean isOngoing()
    +startTimeout()
    +run()
    }
    
    class ForwardingV2{
    -boolean shouldClientRetransmit()
    -boolean boolean isAddrInArrayList()
    -ArrayList<Integer> tracePath()
    +addDirectNeighbour()
    +addContact()
    +getIndexOfAddressInList()
    +decode()
    +encode()
    +toString()    
    }
    
    MyProtocol *-- MediumAccessControl
    MyProtocol *-- TextSplit
    MyProtocol *-- Forwarding
    MyProtocol *-- ForwardingV2
    MyProtocol *-- TimeOut
    MyProtocol <|-- receiveThread
    Forwarding *-- Node
    MyProtocol *-- Node
    MyProtocol o-- Packet
    receiveThread o-- Packet
```