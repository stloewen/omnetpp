//==========================================================================
//   CGATE.H  -  header for
//                     OMNeT++/OMNEST
//            Discrete System Simulation in C++
//
//
//  Declaration of the following classes:
//    cGate       : module gate
//
//==========================================================================

/*--------------------------------------------------------------*
  Copyright (C) 1992-2005 Andras Varga

  This file is distributed WITHOUT ANY WARRANTY. See the file
  `license' for details on this and other legal matters.
*--------------------------------------------------------------*/

#ifndef __CGATE_H
#define __CGATE_H

#include <set>
#include <map>
#include "cobject.h"
#include "cstringpool.h"
#include "opp_string.h"

NAMESPACE_BEGIN

class cGate;
class cModule;
class cMessage;
class cChannelType;
class cChannel;
class cProperties;
class cDisplayString;
class cIdealChannel;
class cBasicChannel;


//
// internal: gateId bitfield macros.
// See note in cgate.cc
//
#define GATEID_LBITS  20
#define GATEID_HBITS  (8*sizeof(int)-GATEID_LBITS)   // usually 12
#define GATEID_HMASK  ((~0)<<GATEID_LBITS)           // usually 0xFFF00000
#define GATEID_LMASK  (~GATEID_HMASK)                // usually 0x000FFFFF

#define MAX_VECTORGATES  ((1<<(GATEID_HBITS-1))-2)   // usually 2046
#define MAX_SCALARGATES  ((1<<(GATEID_LBITS-1))-2)   // usually ~500000
#define MAX_VECTORGATESIZE ((1<<(GATEID_LBITS-1))-1) // usually ~500000

/**
 * Represents a module gate. cGate object are created and managed by modules;
 * the user typically does not want to directly create or destroy cGate
 * objects. However, they are important if a simple module algorithm
 * needs to know about its surroundings.
 *
 * @ingroup SimCore
 */
class SIM_API cGate : public cObject, noncopyable
{
    friend class cModule;
    friend class cModuleGates;
    friend class cPlaceholderModule;

  public:
    /**
     * Gate type
     */
    enum Type {
        NONE = 0,
        INPUT = 'I',
        OUTPUT = 'O',
        INOUT = 'B'
    };

  protected:
    // internal
    struct SIM_API Name
    {
        opp_string name;  // "foo"
        opp_string namei; // "foo$i"
        opp_string nameo; // "foo$o"
        Type type;
        Name(const char *name, Type type);
        bool operator<(const Name& other) const;
    };

  public:
    // Internal data structure, only public for technical reasons (GateIterator).
    // One instance per module and per gate vector/gate pair/gate.
    // Note: gate name and type are factored out to a global pool.
    // Note2: to reduce sizeof(Desc), "size" might be stored in inputgatev[0],
    // although it might not be worthwhile the extra complication and CPU cycles.
    //
    struct Desc
    {
        cModule *ownerp;
        Name *namep;  // pooled
        int size; // gate vector size, or -1 if scalar gate; actually allocated size is capacityFor(size)
        union { cGate *inputgate; cGate **inputgatev; };
        union { cGate *outputgate; cGate **outputgatev; };

        Desc() {ownerp=NULL; size=-1; namep=NULL; inputgate=outputgate=NULL;}
        bool inUse() const {return namep!=NULL;}
        Type type() const {return namep->type;}
        bool isVector() const {return size>=0;}
        int indexOf(const cGate *g) const {return (g->pos>>1)==-1 ? 0 : g->pos>>1;}
        Type typeOf(const cGate *g) const {return (g->pos&1)==0 ? INPUT : OUTPUT;}
        bool isInput(const cGate *g) const {return (g->pos&1)==0;}
        bool isOutput(const cGate *g) const {return (g->pos&1)==1;}
        int gateSize() const {return size>=0 ? size : 1;}
        void setInputGate(cGate *g) {ASSERT(type()!=OUTPUT && !isVector()); inputgate=g; g->desc=this; g->pos=(-1<<1);}
        void setOutputGate(cGate *g) {ASSERT(type()!=INPUT && !isVector()); outputgate=g; g->desc=this; g->pos=(-1<<1)|1;}
        void setInputGate(cGate *g, int index) {ASSERT(type()!=OUTPUT && isVector()); inputgatev[index]=g; g->desc=this; g->pos=(index<<1);}
        void setOutputGate(cGate *g, int index) {ASSERT(type()!=INPUT && isVector()); outputgatev[index]=g; g->desc=this; g->pos=(index<<1)|1;}
        static int capacityFor(int size) {return size<8 ? (size+1)&~1 : size<32 ? (size+3)&~3 : size<256 ? (size+15)&~15 : (size+63)&~63;}
    };

  protected:
    Desc *desc; // descriptor of gate/gate vector, stored in cModule
    int pos;    // b0: input(0) or output(1); rest (pos>>1): array index, or -1 if scalar gate

    cChannel *channelp; // channel object (if exists)
    cGate *fromgatep;   // previous and next gate in the path
    cGate *togatep;

  protected:
    // internal: constructor is protected because only cModule is allowed to create instances
    explicit cGate();

    // also protected: only cModule is allowed to delete gates
    virtual ~cGate();

    // internal
    static void clearFullnamePool();

  public:
    /** @name Redefined cObject member functions */
    //@{
    /**
     * Returns the name of the the gate without the gate index in brackets.
     */
    virtual const char *name() const;

    /**
     * Returns the full name of the gate, which is name() plus the
     * index in square brackets (e.g. "out[4]"). Redefined to add the
     * index.
     */
    virtual const char *fullName() const;

    /**
     * Calls v->visit(this) for each contained object.
     * See cObject for more details.
     */
    virtual void forEachChild(cVisitor *v);

    /**
     * Produces a one-line description of object contents.
     * See cObject for more details.
     */
    virtual std::string info() const;

    /**
     * Returns the owner module of this gate.
     */
    virtual cObject *owner() const;
    //@}

    /**
     * This function is called internally by the send() functions and
     * channel classes' deliver() to deliver the message to its destination.
     * A false return value means that the message object should be deleted
     * by the caller. (This is used e.g. with parallel simulation, for
     * messages leaving the partition.)
     */
    virtual bool deliver(cMessage *msg, simtime_t at);

    /** @name Connecting the gate. */
    //@{

    /**
     * Connects the gate to another gate, optionally using the given
     * channel object. This method can be used to manually create
     * connections for dynamically created modules. If no channel object
     * is specified (or NULL pointer is passed), the existing channel
     * object (assigned via setChannel()) is preserved.
     *
     * If the gate is already connected, an error will occur.
     * The g argument cannot be NULL, that is, you cannot use
     * this function to disconnect a gate. Use disconnect() instead.
     */
    void connectTo(cGate *g, cChannel *chan=NULL);

    /**
     * Disconnects the gate. It also destroys the associated channel object
     * if one has been set (see setChannel()). disconnect() must be invoked
     * on the source gate ("from" side) of the connection.
     *
     * The method has no effect if the gate is not connected.
     */
    void disconnect();
    //@}

    /** @name Accessing the channel object. */
    //@{

    /**
     * Assigns a channel object to this gate. The channel object stores
     * connection attributes such as delay, bit error rate or data rate.
     *
     * See also connectTo().
     */
    void setChannel(cChannel *ch);

    /**
     * Returns the channel object attached to this gate, or NULL if there's
     * no channel. This is the channel between this gate and this->toGate(),
     * that is, channels are stored on the "from" side of the connections.
     */
    cChannel *channel() const {return channelp;}
    //@}

    /** @name Information about the gate. */
    //@{
    /**
     * Returns the gate name without index and potential "$i"/"$o" suffix.
     */
    const char *baseName() const;

    /**
     * Returns the properties for this gate. Properties cannot be changed
     * at runtime.
     */
    cProperties *properties() const;

    /**
     * Returns the gate's type, cGate::INPUT or cGate::OUTPUT. (It never returns
     * cGate::INOUT, because a cGate object is always either the input or
     * the output half of an inout gate ("name$i" or "name$o").
     */
    Type type() const  {return desc->typeOf(this);}

    /**
     * Returns a pointer to the owner module of the gate.
     */
    cModule *ownerModule() const;

    /**
     * Returns the gate ID, which uniquely identifies the gate within the
     * module. IDs are guaranteed to be contiguous within a gate vector:
     * <tt>module->gate(id+index) == module->gate(id)+index</tt>.
     *
     * Gate IDs are stable: they are guaranteed not to change during
     * simulation. (This is a new feature of \opp 4.0. In earlier releases,
     * gate IDs could change when the containing gate vector was resized.)
     *
     * Note: As of \opp 4.0, gate IDs are no longer small integers, and
     * cannot be used for iterating over the gates of a module.
     * Use cModule::GateIterator for iteration.
     */
    int id() const;

    /**
     * Returns true if the gate is part of a gate vector.
     */
    bool isVector() const  {return desc->isVector();}

    /**
     * If the gate is part of a gate vector, returns the gate's index in the vector.
     * Otherwise, it returns 0.
     */
    int index() const  {return desc->indexOf(this);}

    /**
     * If the gate is part of a gate vector, returns the size of the vector.
     * For non-vector gates it returns 1.
     *
     * The gate vector size can also be obtained by calling the cModule::gateSize().
     */
    int size() const  {return desc->gateSize();}
    //@}

    /** @name Transmission state. */
    //@{

    /**
     * If the gate has a channel subclassed from cBasicChannel,
     * the methods calls isBusy() on it and returns the result.
     * Otherwise, it returns false.
     */
    bool isBusy() const;

    /**
     * If the gate has a channel, the method invokes and returns the result of
     * transmissionFinishes() on the channel; otherwise it returns the
     * current simulation time.
     */
    simtime_t transmissionFinishes() const;
    //@}

    /** @name Gate connectivity. */
    //@{

    /**
     * Returns the previous gate in the series of connections (the path) that
     * contains this gate, or a NULL pointer if this gate is the first one in the path.
     * (E.g. for a simple module output gate, this function will return NULL.)
     */
    cGate *fromGate() const {return fromgatep;}

    /**
     * Returns the next gate in the series of connections (the path) that
     * contains this gate, or a NULL pointer if this gate is the last one in the path.
     * (E.g. for a simple module input gate, this function will return NULL.)
     */
    cGate *toGate() const   {return togatep;}

    /**
     * Return the ultimate source of the series of connections
     * (the path) that contains this gate.
     */
    cGate *sourceGate() const;

    /**
     * Return the ultimate destination of the series of connections
     * (the path) that contains this gate.
     */
    cGate *destinationGate() const;

    /**
     * Determines if a given module is in the path containing this gate.
     */
    bool pathContains(cModule *module, int gateId=-1);

    /**
     * Returns true if the gate is connected outside (i.e. to one of its
     * sibling modules or to the parent module).
     *
     * This means that for an input gate, fromGate() must be non-NULL; for an output
     * gate, toGate() must be non-NULL.
     */
    bool isConnectedOutside() const;

    /**
     * Returns true if the gate (of a compound module) is connected inside
     * (i.e. to one of its submodules).
     *
     * This means that for an input gate, toGate() must be non-NULL; for an output
     * gate, fromGate() must be non-NULL.
     */
    bool isConnectedInside() const;

    /**
     * Returns true if the gate fully connected. For a compound module gate
     * this means both isConnectedInside() and isConnectedOutside() are true;
     * for a simple module, only isConnectedOutside() is checked.
     */
    bool isConnected() const;

    /**
     * Returns true if the path (chain of connections) containing this gate
     * starts and ends at a simple module.
     */
    bool isPathOK() const;
    //@}

    /**
     * Returns the display string for the gate, which in practice affects the
     * apprearance of the connection for which this gate is the source.
     * XXX delegates to the channel; if there's no channel, it created a
     * cIdealChannel.
     */
    cDisplayString& displayString();

};

NAMESPACE_END

#endif

