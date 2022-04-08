```mermaid
classDiagram
    direction LR
    
    class MyProtocol {
        +MyProtocol()
        +main(args)
    }
    
    class MediumAccessControl{
        -int MAX_TIMEOUT
        -int IDLE_MULTIPLIER    
        -boolean areWeSending
        -boolean sentRTS
        -boolean wasLastSend
        -int idleCounter
        
        +areWeSending(mediumState, localQueueLength)
    }
    
    class TextSplit{
        +textToBytes(msg)
        +splitTextBytes(msg, size)
    }
    
    class TUI{
        +sendMessage(msg)
    }
    
    class TUIReader{
        +run()
        +parseInput(input)
    }
    
    class Forwarding{
        +init()
        +pathfinding(forwardingTable)
    }
    
    class receiveThread{
        +receiveThread()
        +printByBuffer()
        +run()
    }
    
    MyProtocol *-- MediumAccessControl
    MyProtocol *-- TextSplit
    MyProtocol *-- TUI
    MyProtocol *-- Forwarding
    MyProtocol <|-- receiveThread
    TUI *-- TUIReader
```