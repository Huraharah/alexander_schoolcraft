"""
trainwreck.factory

A factory for the Trainwreck attacks.
"""

from datasets.dataset import Dataset
from trainwreck.attack import TrainTimeDamagingAdversarialAttack
from trainwreck.advreplace import AdversarialReplacementAttack
from trainwreck.jsdswap import JSDSwap
from trainwreck.randomswap import RandomSwap
from trainwreck.trainwreck import TrainwreckAttack


class TrainwreckFactory:
    """
    A factory class that creates the correct attack type for the given attack method ID.
    """

    ATTACK_METHODS = ["advreplace", "jsdswap", "randomswap", "trainwreck"]

    @classmethod
    def attack_obj(
        cls,
        attack_method: str,
        dataset: Dataset,
        poison_rate: float,
        epsilon_px: int,
    ) -> TrainTimeDamagingAdversarialAttack:
        """
        Returns the Trainwreck attack object based on the given attack method ID.
        """
        cls._validate_attack_method(attack_method)

        if attack_method == "advreplace":
            return AdversarialReplacementAttack(
                attack_method, dataset, poison_rate, epsilon_px
            )
        if attack_method == "jsdswap":
            return JSDSwap(attack_method, dataset, poison_rate)
        if attack_method == "randomswap":
            return RandomSwap(attack_method, dataset, poison_rate)
        if attack_method == "trainwreck":
            return TrainwreckAttack(attack_method, dataset, poison_rate, epsilon_px)

        # None of the attacks got returned, yet there was no complaint by the validation method,
        # sounds like a NYI error
        raise NotImplementedError(
            "Factory invoked on a valid attack method, but no actual implemented attack "
            "could be returned. Probably a case of NYI error."
        )

    @classmethod
    def _validate_attack_method(cls, attack_method: str) -> None:
        """
        Raises a ValueError if the validated attack method is not in the list of available
        attack methods.
        """
        if attack_method not in cls.ATTACK_METHODS:
            raise ValueError(f"Invalid attack method '{attack_method}'.")
