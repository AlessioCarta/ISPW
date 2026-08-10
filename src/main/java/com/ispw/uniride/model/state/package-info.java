/**
 * Implementa il Design Pattern State (GoF): {@code RideState} e le sue 4 implementazioni concrete
 * ({@code AvailableState}, {@code FullState}, {@code CompletedState}, {@code CancelledState})
 * incapsulano il comportamento di un {@code Ride} in base al proprio ciclo di vita (posti
 * disponibili o esauriti, viaggio concluso o annullato), evitando condizionali sparsi nell'Entity.
 */
package com.ispw.uniride.model.state;
