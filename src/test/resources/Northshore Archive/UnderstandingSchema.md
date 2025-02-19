# Schema For Block Library

```mermaid
classDiagram
    class BlockTableRecord {
        +String Name
        +Boolean IsAnonymous
        +Boolean IsLayout
        +Point3d Origin
        +Boolean HasAttributeDefinitions
        +Point3d BasePoint
        +String Layer
        +ObjectId[] EntitySet
        +Boolean IsXRef
        +Handle BlockHandle
        +ExtensionDictionary XData
    }

    class BlockReference {
        +Point3d Position
        +Double Rotation
        +Scale3d Scale
        +ObjectId BlockTableRecord
        +ObjectId[] AttributeCollection
        +String Layer
        +Handle EntityHandle
    }

    class AttributeDefinition {
        +String Tag
        +String Prompt
        +String DefaultValue
        +Boolean Constant
        +Boolean Invisible
        +Boolean Verify
        +Point3d Position
        +TextStyle Style
    }

    class AttributeReference {
        +String Tag
        +String TextString
        +Point3d Position
        +Double Rotation
        +TextStyle Style
    }

    BlockTableRecord --> "0..*" AttributeDefinition
    BlockReference --> "1" BlockTableRecord
    BlockReference --> "0..*" AttributeReference
    AttributeReference --> "1" AttributeDefinition
    
```


Let me explain each component of the Block schema:

Block Table Record (Block Definition):
- `Name`: Unique identifier for the block
- `Origin`: Base point coordinates (0,0,0 by default)
- `Layer`: Layer the block was created on
- `Entities`: Collection of objects that make up the block
- `Flags`: IsLayout, IsAnonymous, HasAttributes, etc.
- `XData`: Extended data (custom properties)
- `Handle`: Unique identifier in the DWG database

Block Reference (Block Insert):

Position: Insertion point coordinates
Rotation: Rotation angle in radians
Scale: X, Y, Z scale factors
BlockTableRecord: Reference to block definition
Attributes: Collection of attribute values
Layer: Layer the reference is on
Handle: Unique entity handle


Attribute Definition:

Tag: Attribute identifier
Prompt: User prompt text
DefaultValue: Initial value
Flags: Constant, Invisible, Verify
Position: Text location
Style: Text style properties


Attribute Reference:

Tag: Matches attribute definition tag
TextString: Current value
Position: Actual position in block reference
Style: Text appearance properties


Block-specific Data:

Geometry data for each entity
Visibility state
Dynamic block parameters
Dependencies
External references (if XRef)


Entity Data within Blocks:
CopyEntity {
Handle: Unique identifier
Owner: Block owner handle
Layer: Layer name
Color: Color index or RGB
Linetype: Line type name
Geometry: Entity-specific data
XData: Extended data
}