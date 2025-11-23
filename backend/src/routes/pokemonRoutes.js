const express = require('express');
const router = express.Router();
const pokemonController = require('../controllers/pokemonController');

router.post('/register', pokemonController.registerPokemon);
router.get('/user/:user_login', pokemonController.getUserPokemons);
router.get('/registered/:user_login', pokemonController.checkRegisteredPokemons);
router.put('/:id', pokemonController.updatePokemon);
router.delete('/:id', pokemonController.deletePokemon);
router.get('/details/:id', pokemonController.getPokemonDetails);
router.get('/stats/:user_login', pokemonController.getHomeStats);
router.get('/search/type/:user_login/:type', pokemonController.searchByType);
router.get('/search/ability/:user_login/:ability', pokemonController.searchByAbility);

module.exports = router;